package com.example.photonest.data.repository

import android.net.Uri
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.domain.model.User
import com.example.photonest.domain.model.UserProfile
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val firebaseStorage: FirebaseStorage,
) : IUserRepository {

    override fun getCurrentUser(): Flow<NetworkResult<User?>> = flow {
        emit(NetworkResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            emit(NetworkResult.Success(userDao.getUserById(currentUserId)?.toUser()))

            val remoteResult = safeFirebaseCall {
                val userDoc = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId).get().await()
                val firestoreUser = userDoc.toObject(User::class.java)?.copy(id = currentUserId)
                if (firestoreUser != null) userDao.insertUser(firestoreUser.toEntity())
                firestoreUser
            }

            if (remoteResult is NetworkResult.Success && remoteResult.data != null) {
                emit(remoteResult)
            }
        } else {
            emit(NetworkResult.Success(null))
        }
    }

    override suspend fun getUserById(userId: String): NetworkResult<User?> {
        val result = safeFirebaseCall {
            val userDoc = firestore.collection(Constants.USERS_COLLECTION).document(userId).get().await()
            val firestoreUser = userDoc.toObject(User::class.java)?.copy(id = userId)
            if (firestoreUser != null) userDao.insertUser(firestoreUser.toEntity())
            firestoreUser
        }

        return when (result) {
            is NetworkResult.Success -> if (result.data != null) result else NetworkResult.Success(userDao.getUserById(userId)?.toUser())
            else -> {
                val local = userDao.getUserById(userId)?.toUser()
                if (local != null) NetworkResult.Success(local) else NetworkResult.Error("User not found")
            }
        }
    }

    override suspend fun getUserByUsername(username: String): NetworkResult<User?> {
        val result = safeFirebaseCall {
            val query = firestore.collection(Constants.USERS_COLLECTION).whereEqualTo("username", username).get().await()
            query.documents.firstOrNull()?.toObject(User::class.java)
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> {
                val local = userDao.getUserByUsername(username)?.toUser()
                if (local != null) NetworkResult.Success(local) else NetworkResult.Error("User not found")
            }
        }
    }

    override suspend fun updateUser(user: User): NetworkResult<Unit> = safeFirebaseCall {
        firestore.collection(Constants.USERS_COLLECTION).document(user.id).set(user).await()
        userDao.insertUser(user.toEntity())
    }

    override suspend fun searchUsers(query: String): NetworkResult<List<User>> {
        val result = safeFirebaseCall {
            val results = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("followersCount", Query.Direction.DESCENDING)
                .limit(20).get().await()

            results.documents.mapNotNull { doc -> doc.toObject(User::class.java)?.copy(id = doc.id) }
                .filter { user -> user.username.contains(query, ignoreCase = true) || user.name.contains(query, ignoreCase = true) }
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> NetworkResult.Success(userDao.searchUsers(query, 20).map { it.toUser() })
        }
    }

    override suspend fun followUser(targetUserId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")
        if (currentUserId == targetUserId) return NetworkResult.Error("Cannot follow yourself")


        return safeFirebaseCall {
            val followId = "${currentUserId}_$targetUserId"
            firestore.runBatch { batch ->
                batch.set(firestore.collection(Constants.FOLLOWS_COLLECTION).document(followId), mapOf("followerId" to currentUserId, "followingId" to targetUserId, "timestamp" to System.currentTimeMillis()))
                batch.update(firestore.collection(Constants.USERS_COLLECTION).document(currentUserId), "followingCount", FieldValue.increment(1))
                batch.update(firestore.collection(Constants.USERS_COLLECTION).document(targetUserId), "followersCount", FieldValue.increment(1))
            }.await()
        }
    }

    override suspend fun unfollowUser(targetUserId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")


        return safeFirebaseCall {
            val followId = "${currentUserId}_$targetUserId"
            firestore.runBatch { batch ->
                batch.delete(firestore.collection(Constants.FOLLOWS_COLLECTION).document(followId))
                batch.update(firestore.collection(Constants.USERS_COLLECTION).document(currentUserId), "followingCount", FieldValue.increment(-1))
                batch.update(firestore.collection(Constants.USERS_COLLECTION).document(targetUserId), "followersCount", FieldValue.increment(-1))
            }.await()
        }
    }

    override suspend fun isFollowing(targetUserId: String): NetworkResult<Boolean> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        firestore.collection(Constants.FOLLOWS_COLLECTION).document("${currentUserId}_${targetUserId}").get().await().exists()
    }

    override suspend fun getFollowers(userId: String): NetworkResult<List<User>> = safeFirebaseCall {
        val followQuery = firestore.collection("follows").whereEqualTo("followingId", userId).get().await()
        val followerIds = followQuery.documents.map { it.getString("followerId")!! }

        if (followerIds.isNotEmpty()) {
            val usersQuery = firestore.collection(Constants.USERS_COLLECTION).whereIn("id", followerIds).get().await()
            usersQuery.documents.mapNotNull { doc -> doc.toObject(User::class.java)?.copy(id = doc.id) }
        } else {
            emptyList()
        }
    }

    override suspend fun getFollowing(userId: String): NetworkResult<List<User>> = safeFirebaseCall {
        val followQuery = firestore.collection("follows").whereEqualTo("followerId", userId).get().await()
        val followingIds = followQuery.documents.map { it.getString("followingId")!! }

        if (followingIds.isNotEmpty()) {
            val usersQuery = firestore.collection(Constants.USERS_COLLECTION).whereIn("id", followingIds).get().await()
            usersQuery.documents.mapNotNull { doc -> doc.toObject(User::class.java)?.copy(id = doc.id) }
        } else {
            emptyList()
        }
    }

    override suspend fun getUserProfile(userId: String): NetworkResult<UserProfile> {
        val userResult = getUserById(userId)
        if (userResult !is NetworkResult.Success || userResult.data == null) {
            return NetworkResult.Error("User not found")
        }

        return safeFirebaseCall {
            val posts = postDao.getPostsByUser(userId).map { it.toPost() }
            val currentUserId = firebaseAuth.currentUser?.uid
            val isFollowing = if (currentUserId != null && currentUserId != userId) {
                firestore.collection("follows").document("${currentUserId}_${userId}").get().await().exists()
            } else false

            UserProfile(user = userResult.data, posts = posts, isFollowing = isFollowing, isCurrentUser = currentUserId == userId)
        }
    }

    override suspend fun uploadProfilePicture(imageUri: String): NetworkResult<String> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        val imageRef = firebaseStorage.reference.child(Constants.PROFILE_IMAGES_PATH).child("$currentUserId.jpg")
        imageRef.putFile(Uri.parse(imageUri)).await()
        imageRef.downloadUrl.await().toString()
    }

    override suspend fun getPopularUsers(): NetworkResult<List<User>> {
        val result = safeFirebaseCall {
            val currentUserId = firebaseAuth.currentUser?.uid
            val snapshot = firestore.collection(Constants.USERS_COLLECTION).orderBy("followersCount", Query.Direction.DESCENDING).limit(20).get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }.filter { it.id != currentUserId }
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> NetworkResult.Success(userDao.getPopularUsers(20).map { it.toUser() })
        }
    }

    override suspend fun blockUser(userId: String): NetworkResult<Unit> = NetworkResult.Error("Block user not implemented yet")

    override suspend fun unblockUser(userId: String): NetworkResult<Unit> = NetworkResult.Error("Unblock user not implemented yet")

    override suspend fun getLikedPostIdsByUserId(userId: String): List<String> = try {
        firestore.collection(Constants.LIKES_COLLECTION).whereEqualTo("userId", userId).get().await().documents.mapNotNull { it.getString("postId") }
    } catch (e: Exception) { emptyList() }

    override suspend fun getBookmarkedPostIdsByUserId(userId: String): List<String> = try {
        firestore.collection(Constants.BOOKMARKS_COLLECTION).whereEqualTo("userId", userId).get().await().documents.mapNotNull { it.getString("postId") }
    } catch (e: Exception) { emptyList() }
}