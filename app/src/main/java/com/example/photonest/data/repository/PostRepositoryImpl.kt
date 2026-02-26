package com.example.photonest.data.repository

import android.content.Context
import android.util.Log
import com.example.photonest.core.network.NetworkConnectivityObserver
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.retryCall
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.PendingOperationDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.local.entities.PendingOperationEntity
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.PostDetail
import com.example.photonest.data.model.User
import com.example.photonest.data.sync.SyncWorker
import com.example.photonest.domain.repository.IPostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val pendingOperationDao: PendingOperationDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    @ApplicationContext private val context: Context
) : IPostRepository {

    override fun getPosts(): Flow<NetworkResult<List<Post>>> = flow {
        emit(NetworkResult.Loading())

        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(NetworkResult.Error("Not authenticated"))
            return@flow
        }

        val localPosts = postDao.getAllPosts().map { it.toPost() }
        if (localPosts.isNotEmpty()) {
            emit(NetworkResult.Success(localPosts))
        }

        val remoteResult = safeFirebaseCall {
            val followsSnapshot = retryCall {
                firestore.collection(Constants.FOLLOWS_COLLECTION)
                    .whereEqualTo("followerId", currentUserId)
                    .get().await()
            }

            val followedUserIds = followsSnapshot.documents
                .mapNotNull { it.getString("followingId") }
                .toMutableList()
                .apply { add(currentUserId) }

            if (followedUserIds.isEmpty()) return@safeFirebaseCall emptyList<Post>()

            val allPosts = mutableListOf<Post>()
            followedUserIds.chunked(10).forEach { batch ->
                val snapshot = retryCall {
                    firestore.collection(Constants.POSTS_COLLECTION)
                        .whereIn("userId", batch)
                        .limit(50).get().await()
                }
                allPosts += snapshot.documents.mapNotNull {
                    it.toObject(Post::class.java)?.copy(id = it.id)
                }
            }

            val sortedPosts = allPosts.sortedByDescending { it.timestamp }

            val likedPostIds = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("userId", currentUserId).get().await()
                .documents.mapNotNull { it.getString("postId") }.toSet()

            val bookmarkedPostIds = firestore.collection(Constants.BOOKMARKS_COLLECTION)
                .whereEqualTo("userId", currentUserId).get().await()
                .documents.mapNotNull { it.getString("postId") }.toSet()

            val followedUserSet = followedUserIds.toSet()

            val enrichedPosts = sortedPosts.map { post ->
                post.copy(
                    isLiked = likedPostIds.contains(post.id),
                    isBookmarked = bookmarkedPostIds.contains(post.id),
                    isUserFollowed = post.userId != currentUserId && followedUserSet.contains(post.userId)
                )
            }

            postDao.upsertPosts(enrichedPosts.map { it.toEntity() })
            enrichedPosts
        }

        if (remoteResult is NetworkResult.Success) {
            emit(remoteResult)
        } else if (localPosts.isEmpty()) {
            emit(NetworkResult.Error(remoteResult.message ?: "Failed to load posts"))
        }
    }

    override suspend fun getPostById(postId: String): NetworkResult<PostDetail?> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid

        val postDoc = retryCall { firestore.collection(Constants.POSTS_COLLECTION).document(postId).get().await() }
        val post = postDoc.toObject(Post::class.java)?.copy(id = postId) ?: throw IllegalStateException("Post not found")
        val user = userDao.getUserById(post.userId)?.toUser()

        val isLiked = currentUserId != null && firestore.collection(Constants.LIKES_COLLECTION).document("${currentUserId}_${postId}").get().await().exists()
        val isBookmarked = currentUserId != null && firestore.collection(Constants.BOOKMARKS_COLLECTION).document("${currentUserId}_${postId}").get().await().exists()
        val isFollowing = currentUserId != null && currentUserId != post.userId && firestore.collection(Constants.FOLLOWS_COLLECTION).document("${currentUserId}_${post.userId}").get().await().exists()

        val enrichedPost = post.copy(isLiked = isLiked, isBookmarked = isBookmarked, isUserFollowed = isFollowing)

        PostDetail(post = enrichedPost, user = user, isOwner = currentUserId == post.userId)
    }

    override suspend fun getUserPosts(userId: String): NetworkResult<List<Post>> {
        val result = safeFirebaseCall {
            val query = firestore.collection(Constants.POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            val posts = query.documents.mapNotNull { doc -> doc.toObject(Post::class.java)?.copy(id = doc.id) }
            postDao.insertPosts(posts.map { it.toEntity() })
            posts
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> NetworkResult.Success(postDao.getPostsByUser(userId).map { it.toPost() })
        }
    }

    override suspend fun createPost(post: Post, imageUri: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")

        val imageUrl = if (imageUri.isNotEmpty() && (imageUri.startsWith("content://") || imageUri.startsWith("file://"))) {
            uploadImageToStorage(imageUri)
        } else {
            "https://picsum.photos/400/400?random=${System.currentTimeMillis()}"
        }

        val postId = UUID.randomUUID().toString()
        val newPost = post.copy(id = postId, userId = currentUserId, imageUrl = imageUrl, timestamp = System.currentTimeMillis())

        firestore.collection(Constants.POSTS_COLLECTION).document(postId).set(newPost).await()
        postDao.insertPost(newPost.toEntity())
        firestore.collection(Constants.USERS_COLLECTION).document(currentUserId).update("postsCount", FieldValue.increment(1)).await()
    }

    private suspend fun uploadImageToStorage(imageUri: String): String = withContext(Dispatchers.IO) {
        try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")
            val imageId = UUID.randomUUID().toString()
            val imageRef = firebaseStorage.reference.child("posts").child(currentUserId).child("$imageId.jpg")
            imageRef.putFile(android.net.Uri.parse(imageUri)).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            "https://picsum.photos/400/400?random=${System.currentTimeMillis()}"
        }
    }

    override suspend fun likePost(postId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

        if (!connectivityObserver.isCurrentlyConnected()) {
            pendingOperationDao.insert(PendingOperationEntity(type = "LIKE", targetId = postId))
            SyncWorker.schedule(context)
            return NetworkResult.Success(Unit)
        }

        return safeFirebaseCall {
            val likeDocId = "${currentUserId}_${postId}"
            val existing = firestore.collection(Constants.LIKES_COLLECTION).document(likeDocId).get().await()
            if (!existing.exists()) {
                firestore.runBatch { batch ->
                    batch.set(firestore.collection(Constants.LIKES_COLLECTION).document(likeDocId), mapOf("userId" to currentUserId, "postId" to postId, "timestamp" to System.currentTimeMillis()))
                    batch.update(firestore.collection(Constants.POSTS_COLLECTION).document(postId), "likeCount", FieldValue.increment(1))
                }.await()
            }
        }
    }

    override suspend fun unlikePost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

        if (!connectivityObserver.isCurrentlyConnected()) {
            pendingOperationDao.insert(PendingOperationEntity(type = "UNLIKE", targetId = postId))
            SyncWorker.schedule(context)
            return NetworkResult.Success(Unit)
        }

        return safeFirebaseCall {
            val likeId = "${userId}_$postId"
            firestore.runBatch { batch ->
                batch.delete(firestore.collection(Constants.LIKES_COLLECTION).document(likeId))
                batch.update(firestore.collection(Constants.POSTS_COLLECTION).document(postId), "likeCount", FieldValue.increment(-1))
            }.await()
        }
    }

    override suspend fun bookmarkPost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

        if (!connectivityObserver.isCurrentlyConnected()) {
            pendingOperationDao.insert(PendingOperationEntity(type = "BOOKMARK", targetId = postId))
            SyncWorker.schedule(context)
            return NetworkResult.Success(Unit)
        }

        return safeFirebaseCall {
            firestore.collection(Constants.BOOKMARKS_COLLECTION).document("${userId}_$postId")
                .set(mapOf("userId" to userId, "postId" to postId, "timestamp" to System.currentTimeMillis())).await()
            postDao.updatePostBookmark(postId, true)
        }
    }

    override suspend fun unbookmarkPost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

        if (!connectivityObserver.isCurrentlyConnected()) {
            pendingOperationDao.insert(PendingOperationEntity(type = "UNBOOKMARK", targetId = postId))
            SyncWorker.schedule(context)
            return NetworkResult.Success(Unit)
        }

        return safeFirebaseCall {
            firestore.collection(Constants.BOOKMARKS_COLLECTION).document("${userId}_$postId").delete().await()
            postDao.updatePostBookmark(postId, false)
        }
    }

    override suspend fun getBookmarkedPosts(): NetworkResult<List<Post>> = safeFirebaseCall {
        postDao.getBookmarkedPosts().map { it.toPost() }
    }

    override suspend fun getTrendingPosts(): NetworkResult<List<Post>> {
        val result = safeFirebaseCall {
            val query = firestore.collection(Constants.POSTS_COLLECTION).orderBy("likeCount", Query.Direction.DESCENDING).limit(20).get().await()
            val posts = query.documents.mapNotNull { doc -> doc.toObject(Post::class.java)?.copy(id = doc.id) }
            postDao.insertPosts(posts.map { it.toEntity() })
            posts
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> NetworkResult.Success(postDao.getTrendingPosts(20).map { it.toPost() })
        }
    }

    override suspend fun getPostsByCategory(category: String): NetworkResult<List<Post>> {
        val result = safeFirebaseCall {
            val query = firestore.collection(Constants.POSTS_COLLECTION).whereArrayContains("category", category).orderBy("timestamp", Query.Direction.DESCENDING).limit(50).get().await()
            val posts = query.documents.mapNotNull { doc -> doc.toObject(Post::class.java)?.copy(id = doc.id) }
            postDao.insertPosts(posts.map { it.toEntity() })
            posts
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> {
                val localPosts = postDao.getPostsByCategory(category).map { it.toPost() }
                if (localPosts.isNotEmpty()) NetworkResult.Success(localPosts) else NetworkResult.Error("Failed to load category posts")
            }
        }
    }

    override suspend fun searchPosts(query: String): NetworkResult<List<Post>> {
        val result = safeFirebaseCall {
            val firestoreQuery = firestore.collection(Constants.POSTS_COLLECTION).orderBy("likeCount", Query.Direction.DESCENDING).limit(50).get().await()
            val allPosts = firestoreQuery.documents.mapNotNull { doc -> doc.toObject(Post::class.java)?.copy(id = doc.id) }
            allPosts.filter { post ->
                post.caption.contains(query, ignoreCase = true) || post.tags.any { it.contains(query, ignoreCase = true) } || post.userName.contains(query, ignoreCase = true) || post.location.contains(query, ignoreCase = true) || post.category.any { it.contains(query, ignoreCase = true) }
            }
        }
        return when (result) {
            is NetworkResult.Success -> result
            else -> NetworkResult.Success(postDao.searchPosts(query).map { it.toPost() })
        }
    }

    override suspend fun reportPost(postId: String, reason: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        val reportData = mapOf("postId" to postId, "reporterId" to currentUserId, "reason" to reason, "timestamp" to System.currentTimeMillis(), "status" to "pending")
        firestore.collection(Constants.REPORTS_COLLECTION).add(reportData).await()
    }

    override suspend fun getPostsByIds(postIds: List<String>): NetworkResult<List<Post>> = safeFirebaseCall {
        if (postIds.isEmpty()) return@safeFirebaseCall emptyList<Post>()
        val posts = mutableListOf<Post>()
        postIds.chunked(10).forEach { batch ->
            val query = firestore.collection(Constants.POSTS_COLLECTION).whereIn(FieldPath.documentId(), batch).get().await()
            posts.addAll(query.documents.mapNotNull { doc -> doc.toObject(Post::class.java)?.copy(id = doc.id) })
        }
        postDao.insertPosts(posts.map { it.toEntity() })
        posts
    }

    override suspend fun getUsersWhoLikedPost(postId: String): NetworkResult<List<User>> = safeFirebaseCall {
        val likesSnapshot = firestore.collection(Constants.LIKES_COLLECTION).whereEqualTo("postId", postId).get().await()
        val userIds = likesSnapshot.documents.mapNotNull { it.getString("userId") }
        if (userIds.isEmpty()) return@safeFirebaseCall emptyList<User>()

        val users = mutableListOf<User>()
        userIds.chunked(10).forEach { batch ->
            val usersSnapshot = firestore.collection(Constants.USERS_COLLECTION).whereIn("id", batch).get().await()
            users += usersSnapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }
        }
        users
    }

    override suspend fun deletePost(postId: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        val postDoc = firestore.collection(Constants.POSTS_COLLECTION).document(postId).get().await()
        val post = postDoc.toObject(Post::class.java) ?: throw IllegalStateException("Post not found")

        if (post.userId != currentUserId) throw IllegalStateException("You don't have permission to delete this post")

        if (post.imageUrl.isNotEmpty()) {
            try { firebaseStorage.getReferenceFromUrl(post.imageUrl).delete().await() } catch (e: Exception) { Log.e("PostRepository", "Failed to delete image: ${e.message}") }
        }

        val commentsSnapshot = firestore.collection(Constants.COMMENTS_COLLECTION).whereEqualTo("postId", postId).get().await()
        val batch = firestore.batch()
        commentsSnapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.delete(firestore.collection(Constants.POSTS_COLLECTION).document(postId))
        batch.update(firestore.collection(Constants.USERS_COLLECTION).document(currentUserId), "postsCount", FieldValue.increment(-1))
        batch.commit().await()
        postDao.deletePostById(postId)
    }
}