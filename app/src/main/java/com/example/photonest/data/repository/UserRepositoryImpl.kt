package com.example.photonest.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.photonest.core.network.NetworkConnectivityObserver
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.PendingOperationDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.local.entities.PendingOperationEntity
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import com.example.photonest.data.sync.SyncWorker
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val firebaseStorage: FirebaseStorage,
    private val pendingOperationDao: PendingOperationDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    @ApplicationContext private val context: Context
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

    override suspend fun followUser(targetUserId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        if (currentUserId == targetUserId) {
            return NetworkResult.Error("Cannot follow yourself")
        }

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                val followId = "${currentUserId}_$targetUserId"

                firestore.runBatch { batch ->
                    batch.set(
                        firestore.collection(Constants.FOLLOWS_COLLECTION).document(followId),
                        mapOf(
                            "followerId" to currentUserId,
                            "followingId" to targetUserId,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                    batch.update(
                        firestore.collection(Constants.USERS_COLLECTION).document(currentUserId),
                        "followingCount",
                        FieldValue.increment(1)
                    )
                    batch.update(
                        firestore.collection(Constants.USERS_COLLECTION).document(targetUserId),
                        "followersCount",
                        FieldValue.increment(1)
                    )
                }.await()

                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Follow failed")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(type = "FOLLOW", targetId = targetUserId)
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun unfollowUser(targetUserId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                val followId = "${currentUserId}_$targetUserId"

                firestore.runBatch { batch ->
                    batch.delete(
                        firestore.collection(Constants.FOLLOWS_COLLECTION).document(followId)
                    )
                    batch.update(
                        firestore.collection(Constants.USERS_COLLECTION).document(currentUserId),
                        "followingCount",
                        FieldValue.increment(-1)
                    )
                    batch.update(
                        firestore.collection(Constants.USERS_COLLECTION).document(targetUserId),
                        "followersCount",
                        FieldValue.increment(-1)
                    )
                }.await()

                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Unfollow failed")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(type = "UNFOLLOW", targetId = targetUserId)
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    // Also fix isFollowing() to use point read on follows collection:
    override suspend fun isFollowing(targetUserId: String): NetworkResult<Boolean> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Error("Not authenticated")
            val doc = firestore.collection(Constants.FOLLOWS_COLLECTION)
                .document("${currentUserId}_${targetUserId}")
                .get().await()
            NetworkResult.Success(doc.exists())
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

            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("followersCount", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull {
                it.toObject(User::class.java)?.copy(id = it.id)
            }.filter { it.id != currentUserId }

            NetworkResult.Success(users)
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
            firestore.collection(Constants.BOOKMARKS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("postId") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
