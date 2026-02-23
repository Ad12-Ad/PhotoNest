package com.example.photonest.data.repository

import android.net.Uri
import android.util.Log
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val followDao: FollowDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : IUserRepository {

    override fun getCurrentUser(): Flow<NetworkResult<User?>> = flow {
        emit(NetworkResult.Loading())
        try {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != null) {
                // Try to get from local database first
                val localUser = userDao.getUserById(currentUserId)?.toUser()
                emit(NetworkResult.Success(localUser))

                // Then sync with Firestore
                val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUserId)
                    .get()
                    .await()

                val firestoreUser = userDoc.toObject(User::class.java)?.copy(id = currentUserId)
                if (firestoreUser != null) {
                    userDao.insertUser(firestoreUser.toEntity())
                    emit(NetworkResult.Success(firestoreUser))
                }
            } else {
                emit(NetworkResult.Success(null))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Failed to get current user"))
        }
    }

    override suspend fun getUserById(userId: String): NetworkResult<User?> {
        return try {
            // Try local database first
            val localUser = userDao.getUserById(userId)?.toUser()

            // Then get from Firestore
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val firestoreUser = userDoc.toObject(User::class.java)?.copy(id = userId)
            if (firestoreUser != null) {
                userDao.insertUser(firestoreUser.toEntity())
                NetworkResult.Success(firestoreUser)
            } else {
                NetworkResult.Success(localUser)
            }
        } catch (e: Exception) {
            // Fallback to local data
            val localUser = userDao.getUserById(userId)?.toUser()
            if (localUser != null) {
                NetworkResult.Success(localUser)
            } else {
                NetworkResult.Error(e.message ?: "User not found")
            }
        }
    }

    override suspend fun getUserByUsername(username: String): NetworkResult<User?> {
        return try {
            val query = firestore.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            val user = query.documents.firstOrNull()?.toObject(User::class.java)
            NetworkResult.Success(user)
        } catch (e: Exception) {
            val localUser = userDao.getUserByUsername(username)?.toUser()
            if (localUser != null) {
                NetworkResult.Success(localUser)
            } else {
                NetworkResult.Error(e.message ?: "User not found")
            }
        }
    }

    override suspend fun updateUser(user: User): NetworkResult<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()

            userDao.insertUser(user.toEntity())
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update user")
        }
    }

    override suspend fun searchUsers(query: String): NetworkResult<List<User>> {
        return try {
            val results = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("followersCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = results.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }.filter { user ->
                user.username.contains(query, ignoreCase = true) ||
                        user.name.contains(query, ignoreCase = true)
            }

            NetworkResult.Success(users)
        } catch (e: Exception) {
            val localUsers = userDao.searchUsers(query, 20).map { it.toUser() }
            NetworkResult.Success(localUsers)
        }
    }

    override suspend fun followUser(userId: String): NetworkResult<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Error("Not authenticated")

            if (currentUserId == userId) {
                return NetworkResult.Error("Cannot follow yourself")
            }

            val followId = "${currentUserId}_${userId}"

            // Check if already following
            val existingFollow = firestore.collection("follows")
                .document(followId)
                .get()
                .await()

            if (existingFollow.exists()) {
                return NetworkResult.Error("Already following this user")
            }

            val batch = firestore.batch()

            val followData = hashMapOf(
                "id" to followId,
                "followerId" to currentUserId,
                "followingId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            val followRef = firestore.collection("follows").document(followId)
            batch.set(followRef, followData)

            val currentUserRef = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId)
            batch.update(currentUserRef, "followingCount", FieldValue.increment(1))

            val targetUserRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
            batch.update(targetUserRef, "followersCount", FieldValue.increment(1))

            batch.commit().await()

            Log.d("UserRepository", "Follow successful: $followId")

            try {
                val currentUserDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUserId)
                    .get()
                    .await()

                val currentUser = currentUserDoc.toObject(User::class.java)

                val notificationData = hashMapOf(
                    "userId" to userId,
                    "fromUserId" to currentUserId,
                    "fromUsername" to (currentUser?.username ?: "Someone"),
                    "fromUserImage" to (currentUser?.profilePicture ?: ""),
                    "type" to "FOLLOW",
                    "message" to "${currentUser?.username ?: "Someone"} started following you",
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false,
                    "isClicked" to false
                )

                firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                    .add(notificationData)
                    .await()
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to create notification: ${e.message}")
            }

            // Update local database
            userDao.getUserById(currentUserId)?.let { user ->
                userDao.insertUser(user.copy(followingCount = user.followingCount + 1))
            }

            userDao.getUserById(userId)?.let { user ->
                userDao.insertUser(user.copy(followersCount = user.followersCount + 1))
            }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Follow failed: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Failed to follow user")
        }
    }

    override suspend fun unfollowUser(userId: String): NetworkResult<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Error("Not authenticated")

            val followId = "${currentUserId}_${userId}"

            val batch = firestore.batch()

            val followRef = firestore.collection("follows").document(followId)
            batch.delete(followRef)

            val currentUserRef = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId)
            batch.update(currentUserRef, "followingCount", FieldValue.increment(-1))

            val targetUserRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
            batch.update(targetUserRef, "followersCount", FieldValue.increment(-1))

            batch.commit().await()

            postDao.deletePostsByUser(userId)

            Log.d("UserRepository", "Unfollow successful: $followId")

            // Update local database
            userDao.getUserById(currentUserId)?.let { user ->
                userDao.insertUser(user.copy(followingCount = maxOf(0, user.followingCount - 1)))
            }

            userDao.getUserById(userId)?.let { user ->
                userDao.insertUser(user.copy(followersCount = maxOf(0, user.followersCount - 1)))
            }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Unfollow failed: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Failed to unfollow user")
        }
    }


//    override suspend fun followUser(userId: String): Resource<Unit> {
//        return try {
//            val currentUserId = firebaseAuth.currentUser?.uid
//                ?: return Resource.Error("Not authenticated")
//
//            if (currentUserId == userId) {
//                return Resource.Error("Cannot follow yourself")
//            }
//
//            val followId = "${currentUserId}_${userId}"
//
//            val existingFollow = firestore.collection("follows")
//                .document(followId)
//                .get()
//                .await()
//
//            if (existingFollow.exists()) {
//                return Resource.Error("Already following this user")
//            }
//
//            val batch = firestore.batch()
//
//            // 1. Create follow document
//            val followData = hashMapOf(
//                "id" to followId,
//                "followerId" to currentUserId,
//                "followingId" to userId,
//                "timestamp" to System.currentTimeMillis()
//            )
//
//            val followRef = firestore.collection("follows").document(followId)
//            batch.set(followRef, followData)
//
//            val currentUserRef = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId)
//            batch.update(currentUserRef, mapOf(
//                "followingCount" to FieldValue.increment(1),
//                "following" to FieldValue.arrayUnion(userId)
//            ))
//
//            val targetUserRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
//            batch.update(targetUserRef, mapOf(
//                "followersCount" to FieldValue.increment(1),
//                "followers" to FieldValue.arrayUnion(currentUserId)
//            ))
//
//            batch.commit().await()
//
//            Log.d("UserRepository", "✅ Follow successful: $followId")
//
//            try {
//                val currentUserDoc = firestore.collection(Constants.USERS_COLLECTION)
//                    .document(currentUserId)
//                    .get()
//                    .await()
//
//                val currentUser = currentUserDoc.toObject(User::class.java)
//
//                val notificationData = hashMapOf(
//                    "userId" to userId,
//                    "fromUserId" to currentUserId,
//                    "fromUsername" to (currentUser?.username ?: "Someone"),
//                    "fromUserImage" to (currentUser?.profilePicture ?: ""),
//                    "type" to "FOLLOW",
//                    "message" to "${currentUser?.username ?: "Someone"} started following you",
//                    "timestamp" to System.currentTimeMillis(),
//                    "isRead" to false,
//                    "isClicked" to false
//                )
//
//                firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
//                    .add(notificationData)
//                    .await()
//
//                Log.d("UserRepository", "Notification created for follow")
//            } catch (e: Exception) {
//                Log.e("UserRepository", "Failed to create notification: ${e.message}")
//            }
//
//            userDao.getUserById(currentUserId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(followingCount = user.followingCount + 1)
//                )
//            }
//
//            userDao.getUserById(userId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(followersCount = user.followersCount + 1)
//                )
//            }
//
//            Resource.Success(Unit)
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Follow failed: ${e.message}", e)
//            Resource.Error(e.message ?: "Failed to follow user")
//        }
//    }
//
//    override suspend fun unfollowUser(userId: String): Resource<Unit> {
//        return try {
//            val currentUserId = firebaseAuth.currentUser?.uid
//                ?: return Resource.Error("Not authenticated")
//
//            val followId = "${currentUserId}_${userId}"
//
//            val batch = firestore.batch()
//
//            val followRef = firestore.collection("follows").document(followId)
//            batch.delete(followRef)
//
//            val currentUserRef = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId)
//            batch.update(currentUserRef, mapOf(
//                "followingCount" to FieldValue.increment(-1),
//                "following" to FieldValue.arrayRemove(userId)
//            ))
//
//            val targetUserRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
//            batch.update(targetUserRef, mapOf(
//                "followersCount" to FieldValue.increment(-1),
//                "followers" to FieldValue.arrayRemove(currentUserId)
//            ))
//
//            batch.commit().await()
//
//            Log.d("UserRepository", "Unfollow successful: $followId")
//
//            // Update local database
//            userDao.getUserById(currentUserId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(followingCount = maxOf(0, user.followingCount - 1))
//                )
//            }
//
//            userDao.getUserById(userId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(followersCount = maxOf(0, user.followersCount - 1))
//                )
//            }
//
//            Resource.Success(Unit)
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Unfollow failed: ${e.message}", e)
//            Resource.Error(e.message ?: "Failed to unfollow user")
//        }
//    }


//    override suspend fun followUser(userId: String): Resource<Unit> {
//        return try {
//            val currentUserId = firebaseAuth.currentUser?.uid
//                ?: return Resource.Error("Not authenticated")
//
//            if (currentUserId == userId) {
//                return Resource.Error("Cannot follow yourself")
//            }
//
//            val followId = "${currentUserId}_${userId}"
//
//            // Check if already following
//            val existingFollow = firestore.collection("follows")
//                .document(followId)
//                .get()
//                .await()
//
//            if (existingFollow.exists()) {
//                return Resource.Error("Already following this user")
//            }
//
//            // Create follow document
//            val followData = hashMapOf(
//                "id" to followId,
//                "followerId" to currentUserId,
//                "followingId" to userId,
//                "timestamp" to System.currentTimeMillis()
//            )
//
//            firestore.collection("follows")
//                .document(followId)
//                .set(followData)
//                .await()
//
//            firestore.collection(Constants.USERS_COLLECTION)
//                .document(currentUserId)
//                .update(
//                    mapOf(
//                        "followingCount" to FieldValue.increment(1),
//                        "following" to FieldValue.arrayUnion(userId)  // ADD THIS
//                    )
//                )
//                .await()
//
//            firestore.collection(Constants.USERS_COLLECTION)
//                .document(userId)
//                .update(
//                    mapOf(
//                        "followersCount" to FieldValue.increment(1),
//                        "followers" to FieldValue.arrayUnion(currentUserId)  // ADD THIS
//                    )
//                )
//                .await()
//
//            try {
//                val currentUserDoc = firestore.collection(Constants.USERS_COLLECTION)
//                    .document(currentUserId)
//                    .get()
//                    .await()
//
//                val currentUser = currentUserDoc.toObject(User::class.java)
//
//                val notificationData = hashMapOf(
//                    "userId" to userId,
//                    "fromUserId" to currentUserId,
//                    "fromUsername" to (currentUser?.username ?: "Someone"),
//                    "fromUserImage" to (currentUser?.profilePicture ?: ""),
//                    "type" to "FOLLOW",
//                    "message" to "${currentUser?.username ?: "Someone"} started following you",
//                    "timestamp" to System.currentTimeMillis(),
//                    "isRead" to false,
//                    "isClicked" to false
//                )
//
//                // Add notification to Firestore
//                firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
//                    .add(notificationData)
//                    .await()
//
//                Log.d("UserRepository", "Notification created for follow")
//            } catch (e: Exception) {
//                Log.e("UserRepository", "Failed to create notification: ${e.message}")
//            }
//
//            userDao.insertUser(
//                userDao.getUserById(currentUserId)?.copy(followingCount =
//                    (userDao.getUserById(currentUserId)?.followingCount ?: 0) + 1)
//                    ?: return Resource.Success(Unit)
//            )
//
//            userDao.insertUser(
//                userDao.getUserById(userId)?.copy(followersCount =
//                    (userDao.getUserById(userId)?.followersCount ?: 0) + 1)
//                    ?: return Resource.Success(Unit)
//            )
//
//            Resource.Success(Unit)
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to follow user")
//        }
//    }
//
//    override suspend fun unfollowUser(userId: String): Resource<Unit> {
//        return try {
//            val currentUserId = firebaseAuth.currentUser?.uid
//                ?: return Resource.Error("Not authenticated")
//
//            val followId = "${currentUserId}_${userId}"
//
//            firestore.collection("follows")
//                .document(followId)
//                .delete()
//                .await()
//
//            firestore.collection(Constants.USERS_COLLECTION)
//                .document(currentUserId)
//                .update(
//                    mapOf(
//                        "followingCount" to FieldValue.increment(-1),
//                        "following" to FieldValue.arrayRemove(userId)  // ADD THIS
//                    )
//                )
//                .await()
//
//            firestore.collection(Constants.USERS_COLLECTION)
//                .document(userId)
//                .update(
//                    mapOf(
//                        "followersCount" to FieldValue.increment(-1),
//                        "followers" to FieldValue.arrayRemove(currentUserId)  // ADD THIS
//                    )
//                )
//                .await()
//
//            userDao.getUserById(currentUserId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(
//                        followingCount = maxOf(0, user.followingCount - 1)
//                    )
//                )
//            }
//
//            userDao.getUserById(userId)?.let { user ->
//                userDao.insertUser(
//                    user.copy(
//                        followersCount = maxOf(0, user.followersCount - 1)
//                    )
//                )
//            }
//
//            Resource.Success(Unit)
//        } catch (e: Exception) {
//            Resource.Error(e.message ?: "Failed to unfollow user")
//        }
//    }

    override suspend fun isFollowing(userId: String): NetworkResult<Boolean> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Success(false)

            if (currentUserId == userId) {
                return NetworkResult.Success(false) // Can't follow yourself
            }

            val followId = "${currentUserId}_${userId}"
            val followDoc = firestore.collection("follows")
                .document(followId)
                .get()
                .await()

            NetworkResult.Success(followDoc.exists())
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to check follow status")
        }
    }


    override suspend fun getFollowers(userId: String): NetworkResult<List<User>> {
        return try {
            val followQuery = firestore.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followerIds = followQuery.documents.map { it.getString("followerId")!! }

            if (followerIds.isNotEmpty()) {
                val usersQuery = firestore.collection(Constants.USERS_COLLECTION)
                    .whereIn("id", followerIds)
                    .get()
                    .await()

                val users = usersQuery.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }
                NetworkResult.Success(users)
            } else {
                NetworkResult.Success(emptyList())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get followers")
        }
    }

    override suspend fun getFollowing(userId: String): NetworkResult<List<User>> {
        return try {
            val followQuery = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = followQuery.documents.map { it.getString("followingId")!! }

            if (followingIds.isNotEmpty()) {
                val usersQuery = firestore.collection(Constants.USERS_COLLECTION)
                    .whereIn("id", followingIds)
                    .get()
                    .await()

                val users = usersQuery.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                }
                NetworkResult.Success(users)
            } else {
                NetworkResult.Success(emptyList())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get following")
        }
    }

    override suspend fun getUserProfile(userId: String): NetworkResult<UserProfile> {
        return try {
            val user = getUserById(userId)
            if (user is NetworkResult.Success && user.data != null) {
                val posts = postDao.getPostsByUser(userId).map { it.toPost() }
                val currentUserId = firebaseAuth.currentUser?.uid
                val isFollowing = if (currentUserId != null && currentUserId != userId) {
                    val followId = "${currentUserId}_${userId}"
                    val followDoc = firestore.collection("follows")
                        .document(followId)
                        .get()
                        .await()
                    followDoc.exists()
                } else false

                val userProfile = UserProfile(
                    user = user.data,
                    posts = posts,
                    isFollowing = isFollowing,
                    isCurrentUser = currentUserId == userId
                )
                NetworkResult.Success(userProfile)
            } else {
                NetworkResult.Error("User not found")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get user profile")
        }
    }

    override suspend fun uploadProfilePicture(imageUri: String): NetworkResult<String> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Error("Not authenticated")

            val imageRef = firebaseStorage.reference
                .child(Constants.PROFILE_IMAGES_PATH)
                .child("$currentUserId.jpg")

            val uri = Uri.parse(imageUri)

            Log.d("UserRepository", "Uploading profile picture from: $imageUri")

            val uploadTask = imageRef.putFile(uri).await()

            Log.d("UserRepository", "Upload successful, getting download URL")

            val downloadUrl = imageRef.downloadUrl.await()

            Log.d("UserRepository", "Download URL: ${downloadUrl.toString()}")

            NetworkResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("UserRepository", "Profile picture upload failed: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Failed to upload profile picture")
        }
    }



    override suspend fun getPopularUsers(): NetworkResult<List<User>> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid

            val query = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("followersCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = query.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }

            val enrichedUsers = users.map { user ->
                if (currentUserId != null && user.id != currentUserId) {
                    val followId = "${currentUserId}_${user.id}"
                    val isFollowingDoc = firestore.collection("follows")
                        .document(followId)
                        .get()
                        .await()

                    user.copy(
                        followers = if (isFollowingDoc.exists())
                            listOf(currentUserId) else emptyList()
                    )
                } else {
                    user
                }
            }.filter { it.id != currentUserId }

            NetworkResult.Success(enrichedUsers)
        } catch (e: Exception) {
            val localUsers = userDao.getPopularUsers(20).map { it.toUser() }
            NetworkResult.Success(localUsers)
        }
    }

    override suspend fun blockUser(userId: String): NetworkResult<Unit> {
        return try {
            // Implementation for blocking user
            NetworkResult.Error("Block user not implemented yet")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to block user")
        }
    }

    override suspend fun unblockUser(userId: String): NetworkResult<Unit> {
        return try {
            // Implementation for unblocking user
            NetworkResult.Error("Unblock user not implemented yet")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to unblock user")
        }
    }

    override suspend fun getLikedPostIdsByUserId(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("postId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBookmarkedPostIdsByUserId(userId: String): List<String> {
        return try {
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            userDoc.get("bookmarks") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


}
