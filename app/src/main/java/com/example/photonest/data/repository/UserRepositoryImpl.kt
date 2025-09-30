package com.example.photonest.data.repository

import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.data.model.Follow
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
import java.util.UUID
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

    override fun getCurrentUser(): Flow<Resource<User?>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != null) {
                // Try to get from local database first
                val localUser = userDao.getUserById(currentUserId)?.toUser()
                emit(Resource.Success(localUser))

                // Then sync with Firestore
                val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUserId)
                    .get()
                    .await()

                val firestoreUser = userDoc.toObject(User::class.java)?.copy(id = currentUserId)
                if (firestoreUser != null) {
                    userDao.insertUser(firestoreUser.toEntity())
                    emit(Resource.Success(firestoreUser))
                }
            } else {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get current user"))
        }
    }

    override suspend fun getUserById(userId: String): Resource<User?> {
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
                Resource.Success(firestoreUser)
            } else {
                Resource.Success(localUser)
            }
        } catch (e: Exception) {
            // Fallback to local data
            val localUser = userDao.getUserById(userId)?.toUser()
            if (localUser != null) {
                Resource.Success(localUser)
            } else {
                Resource.Error(e.message ?: "User not found")
            }
        }
    }

    override suspend fun getUserByUsername(username: String): Resource<User?> {
        return try {
            val query = firestore.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            val user = query.documents.firstOrNull()?.toObject(User::class.java)
            Resource.Success(user)
        } catch (e: Exception) {
            val localUser = userDao.getUserByUsername(username)?.toUser()
            if (localUser != null) {
                Resource.Success(localUser)
            } else {
                Resource.Error(e.message ?: "User not found")
            }
        }
    }

    override suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()

            userDao.insertUser(user.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user")
        }
    }

    override suspend fun searchUsers(query: String): Resource<List<User>> {
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

            Resource.Success(users)
        } catch (e: Exception) {
            val localUsers = userDao.searchUsers(query, 20).map { it.toUser() }
            Resource.Success(localUsers)
        }
    }

    override suspend fun followUser(userId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            val followId = UUID.randomUUID().toString()
            val follow = Follow(
                id = followId,
                followerId = currentUserId,
                followingId = userId,
                timestamp = System.currentTimeMillis()
            )

            // Add to Firestore
            firestore.collection("follows")
                .document(followId)
                .set(follow)
                .await()

            // Update follower/following counts
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUserId)
                .update("followingCount", FieldValue.increment(1))
                .await()

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("followersCount", FieldValue.increment(1))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to follow user")
        }
    }

    override suspend fun unfollowUser(userId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            // Remove from Firestore
            val followQuery = firestore.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            followQuery.documents.forEach { doc ->
                doc.reference.delete()
            }

            // Update follower/following counts
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUserId)
                .update("followingCount", FieldValue.increment(-1))
                .await()

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("followersCount", FieldValue.increment(-1))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unfollow user")
        }
    }

    override suspend fun getFollowers(userId: String): Resource<List<User>> {
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
                Resource.Success(users)
            } else {
                Resource.Success(emptyList())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get followers")
        }
    }

    override suspend fun getFollowing(userId: String): Resource<List<User>> {
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
                Resource.Success(users)
            } else {
                Resource.Success(emptyList())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get following")
        }
    }

    override suspend fun getUserProfile(userId: String): Resource<UserProfile> {
        return try {
            val user = getUserById(userId)
            if (user is Resource.Success && user.data != null) {
                val posts = postDao.getPostsByUser(userId).map { it.toPost() }
                val currentUserId = firebaseAuth.currentUser?.uid
                val isFollowing = if (currentUserId != null) {
                    followDao.isFollowing(currentUserId, userId)
                } else false

                val userProfile = UserProfile(
                    user = user.data,
                    posts = posts,
                    isFollowing = isFollowing,
                    isCurrentUser = currentUserId == userId
                )
                Resource.Success(userProfile)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user profile")
        }
    }

    override suspend fun uploadProfilePicture(imageUri: String): Resource<String> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            val imageRef = firebaseStorage.reference
                .child("profile_pictures")
                .child("$currentUserId.jpg")

            // This is a placeholder - you would implement actual image upload here
            Resource.Error("Image upload not implemented yet")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload profile picture")
        }
    }

    override suspend fun getPopularUsers(): Resource<List<User>> {
        return try {
            val query = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("followersCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = query.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }

            Resource.Success(users)
        } catch (e: Exception) {
            val localUsers = userDao.getPopularUsers(20).map { it.toUser() }
            Resource.Success(localUsers)
        }
    }

    override suspend fun blockUser(userId: String): Resource<Unit> {
        return try {
            // Implementation for blocking user
            Resource.Error("Block user not implemented yet")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to block user")
        }
    }

    override suspend fun unblockUser(userId: String): Resource<Unit> {
        return try {
            // Implementation for unblocking user
            Resource.Error("Unblock user not implemented yet")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unblock user")
        }
    }
}
