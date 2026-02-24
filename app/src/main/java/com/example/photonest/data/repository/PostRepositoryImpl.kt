package com.example.photonest.data.repository

import android.content.Context
import android.util.Log
import com.example.photonest.core.network.NetworkConnectivityObserver
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
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

        try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: run {
                    emit(NetworkResult.Error("Not authenticated"))
                    return@flow
                }

            // Emit cached posts first (offline-first)
            val localPosts = postDao.getAllPosts().map { it.toPost() }
            if (localPosts.isNotEmpty()) {
                emit(NetworkResult.Success(localPosts))
            }

            // Fetch followed users
            val followsSnapshot = firestore.collection(Constants.FOLLOWS_COLLECTION)
                .whereEqualTo("followerId", currentUserId)
                .get()
                .await()

            val followedUserIds = followsSnapshot.documents
                .mapNotNull { it.getString("followingId") }
                .toMutableList()
                .apply { add(currentUserId) }

            if (followedUserIds.isEmpty()) {
                emit(NetworkResult.Success(emptyList()))
                return@flow
            }

            // Fetch posts
            val allPosts = mutableListOf<Post>()
            followedUserIds.chunked(10).forEach { batch ->
                val snapshot = firestore.collection(Constants.POSTS_COLLECTION)
                    .whereIn("userId", batch)
                    .limit(50)
                    .get()
                    .await()

                allPosts += snapshot.documents.mapNotNull {
                    it.toObject(Post::class.java)?.copy(id = it.id)
                }
            }

            val sortedPosts = allPosts.sortedByDescending { it.timestamp }

            // Fetch likes of current user (ONE query)
            val likedPostIds = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("postId") }
                .toSet()

            //  Fetch bookmarks (ONE query)
            val bookmarkedPostIds = firestore.collection(Constants.BOOKMARKS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("postId") }
                .toSet()

            val followedUserSet = followedUserIds.toSet()

            val enrichedPosts = sortedPosts.map { post ->
                post.copy(
                    isLiked = likedPostIds.contains(post.id),
                    isBookmarked = bookmarkedPostIds.contains(post.id),
                    isUserFollowed = post.userId != currentUserId &&
                            followedUserSet.contains(post.userId)
                )
            }

            postDao.upsertPosts(enrichedPosts.map { it.toEntity() })
            emit(NetworkResult.Success(enrichedPosts))

        } catch (e: Exception) {
            val cached = postDao.getAllPosts().map { it.toPost() }
            if (cached.isNotEmpty()) {
                emit(NetworkResult.Success(cached))
            } else {
                emit(NetworkResult.Error(e.message ?: "Failed to load posts"))
            }
        }
    }

    override suspend fun getPostById(postId: String): NetworkResult<PostDetail?> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid

            val postDoc = firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .get()
                .await()

            val post = postDoc.toObject(Post::class.java)?.copy(id = postId)
                ?: return NetworkResult.Error("Post not found")

            val user = userDao.getUserById(post.userId)?.toUser()

            val isLiked = currentUserId != null &&
                    firestore.collection(Constants.LIKES_COLLECTION)
                        .document("${currentUserId}_${postId}")
                        .get()
                        .await()
                        .exists()

            val isBookmarked = currentUserId != null &&
                    firestore.collection(Constants.BOOKMARKS_COLLECTION)
                        .document("${currentUserId}_${postId}")
                        .get()
                        .await()
                        .exists()

            val isFollowing = currentUserId != null &&
                    currentUserId != post.userId &&
                    firestore.collection(Constants.FOLLOWS_COLLECTION)
                        .document("${currentUserId}_${post.userId}")
                        .get()
                        .await()
                        .exists()

            val enrichedPost = post.copy(
                isLiked = isLiked,
                isBookmarked = isBookmarked,
                isUserFollowed = isFollowing
            )

            NetworkResult.Success(
                PostDetail(
                    post = enrichedPost,
                    user = user,
                    isOwner = currentUserId == post.userId
                )
            )

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to load post")
        }
    }

    override suspend fun getUserPosts(userId: String): NetworkResult<List<Post>> {
        return try {
            val query = firestore.collection(Constants.POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = query.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            postDao.insertPosts(posts.map { it.toEntity() })
            NetworkResult.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getPostsByUser(userId).map { it.toPost() }
            NetworkResult.Success(localPosts)
        }
    }

    override suspend fun createPost(post: Post, imageUri: String): NetworkResult<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

            Log.d("PostRepository", "Creating post with imageUri: $imageUri")

            // Upload image to Firebase Storage if imageUri is provided
            val imageUrl = if (imageUri.isNotEmpty() && imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                Log.d("PostRepository", "Uploading image to Firebase Storage")
                uploadImageToStorage(imageUri)
            } else {
                Log.d("PostRepository", "Using placeholder image")
                // Use placeholder image if no image selected
                "https://picsum.photos/400/400?random=${System.currentTimeMillis()}"
            }

            Log.d("PostRepository", "Final imageUrl: $imageUrl")

            val postId = UUID.randomUUID().toString()
            val newPost = post.copy(
                id = postId,
                userId = currentUserId,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )

            // Save to Firestore
            firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .set(newPost)
                .await()

            // Update local database
            postDao.insertPost(newPost.toEntity())

            // Update user's post count
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUserId)
                .update("postsCount", FieldValue.increment(1))
                .await()

            Log.d("PostRepository", "Post created successfully")
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "Failed to create post: ${e.message}")
            NetworkResult.Error(e.message ?: "Failed to create post")
        }
    }

    private suspend fun uploadImageToStorage(imageUri: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("Not authenticated")
                val imageId = UUID.randomUUID().toString()

                // Create Firebase Storage reference
                val imageRef = firebaseStorage.reference
                    .child("posts")
                    .child(currentUserId)
                    .child("$imageId.jpg")

                // Upload image directly from URI
                val uploadTask = imageRef.putFile(android.net.Uri.parse(imageUri)).await()

                // Get download URL
                val downloadUrl = imageRef.downloadUrl.await()

                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e("PostRepository", "Image upload failed: ${e.message}")
                // Return placeholder on error
                "https://picsum.photos/400/400?random=${System.currentTimeMillis()}"
            }
        }
    }

    override suspend fun likePost(postId: String): NetworkResult<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                val likeDocId = "${currentUserId}_${postId}"

                val existing = firestore.collection(Constants.LIKES_COLLECTION)
                    .document(likeDocId)
                    .get()
                    .await()

                if (existing.exists()) {
                    return NetworkResult.Success(Unit)
                }

                firestore.runBatch { batch ->
                    batch.set(
                        firestore.collection(Constants.LIKES_COLLECTION).document(likeDocId),
                        mapOf(
                            "userId" to currentUserId,
                            "postId" to postId,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                    batch.update(
                        firestore.collection(Constants.POSTS_COLLECTION).document(postId),
                        "likeCount",
                        FieldValue.increment(1)
                    )
                }.await()

                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Failed to like post")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(
                    type = "LIKE",
                    targetId = postId
                )
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun unlikePost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                val likeId = "${userId}_$postId"
                firestore.runBatch { batch ->
                    batch.delete(
                        firestore.collection(Constants.LIKES_COLLECTION).document(likeId)
                    )
                    batch.update(
                        firestore.collection(Constants.POSTS_COLLECTION).document(postId),
                        "likeCount",
                        FieldValue.increment(-1)
                    )
                }.await()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Unlike failed")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(type = "UNLIKE", targetId = postId)
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun bookmarkPost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                firestore.collection(Constants.BOOKMARKS_COLLECTION)
                    .document("${userId}_$postId")
                    .set(mapOf(
                        "userId" to userId,
                        "postId" to postId,
                        "timestamp" to System.currentTimeMillis()
                    )).await()

                postDao.updatePostBookmark(postId, true)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Bookmark failed")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(type = "BOOKMARK", targetId = postId)
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun unbookmarkPost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid
            ?: return NetworkResult.Error("Not authenticated")

        return if (connectivityObserver.isCurrentlyConnected()) {
            try {
                firestore.collection(Constants.BOOKMARKS_COLLECTION)
                    .document("${userId}_$postId")
                    .delete()
                    .await()

                postDao.updatePostBookmark(postId, false)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(e.message ?: "Unbookmark failed")
            }
        } else {
            pendingOperationDao.insert(
                PendingOperationEntity(type = "UNBOOKMARK", targetId = postId)
            )
            SyncWorker.schedule(context)
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun getBookmarkedPosts(): NetworkResult<List<Post>> {
        return try {
            val posts = postDao.getBookmarkedPosts().map { it.toPost() }
            NetworkResult.Success(posts)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get bookmarked posts")
        }
    }

    override suspend fun getTrendingPosts(): NetworkResult<List<Post>> {
        return try {
            val query = firestore.collection(Constants.POSTS_COLLECTION)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val posts = query.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            postDao.insertPosts(posts.map { it.toEntity() })
            NetworkResult.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getTrendingPosts(20).map { it.toPost() }
            NetworkResult.Success(localPosts)
        }
    }

    override suspend fun getPostsByCategory(category: String): NetworkResult<List<Post>> {
        return try {
            val query = firestore.collection(Constants.POSTS_COLLECTION)
                .whereArrayContains("category", category)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val posts = query.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            postDao.insertPosts(posts.map { it.toEntity() })
            NetworkResult.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getPostsByCategory(category).map { it.toPost() }
            if (localPosts.isNotEmpty()) {
                NetworkResult.Success(localPosts)
            } else {
                NetworkResult.Error(e.message ?: "Failed to load category posts")
            }
        }
    }

    override suspend fun searchPosts(query: String): NetworkResult<List<Post>> {
        return try {
            val firestoreQuery = firestore.collection(Constants.POSTS_COLLECTION)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val allPosts = firestoreQuery.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            val filteredPosts = allPosts.filter { post ->
                post.caption.contains(query, ignoreCase = true) ||
                        post.tags.any { it.contains(query, ignoreCase = true) } ||
                        post.userName.contains(query, ignoreCase = true) ||
                        post.location.contains(query, ignoreCase = true) ||
                        post.category.any { it.contains(query, ignoreCase = true) }
            }

            NetworkResult.Success(filteredPosts)
        } catch (e: Exception) {
            val localPosts = postDao.searchPosts(query).map { it.toPost() }
            NetworkResult.Success(localPosts)
        }
    }

    override suspend fun reportPost(postId: String, reason: String): NetworkResult<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

            val reportData = mapOf(
                "postId" to postId,
                "reporterId" to currentUserId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending"
            )

            firestore.collection(Constants.REPORTS_COLLECTION)
                .add(reportData)
                .await()

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to report post")
        }
    }
    override suspend fun getPostsByIds(postIds: List<String>): NetworkResult<List<Post>> {
        return try {
            if (postIds.isEmpty()) {
                return NetworkResult.Success(emptyList())
            }

            val posts = mutableListOf<Post>()

            val batches = postIds.chunked(10)
            for (batch in batches) {
                val query = firestore.collection(Constants.POSTS_COLLECTION)
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .await()

                val batchPosts = query.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                posts.addAll(batchPosts)
            }

            postDao.insertPosts(posts.map { it.toEntity() })

            NetworkResult.Success(posts)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get posts by IDs")
        }
    }

    override suspend fun getUsersWhoLikedPost(postId: String): NetworkResult<List<User>> {
        return try {
            val likesSnapshot = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val userIds = likesSnapshot.documents
                .mapNotNull { it.getString("userId") }

            if (userIds.isEmpty()) {
                return NetworkResult.Success(emptyList())
            }

            val users = mutableListOf<User>()
            userIds.chunked(10).forEach { batch ->
                val usersSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                    .whereIn("id", batch)
                    .get()
                    .await()

                users += usersSnapshot.documents.mapNotNull {
                    it.toObject(User::class.java)?.copy(id = it.id)
                }
            }

            NetworkResult.Success(users)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to load users who liked post")
        }
    }

    // Also update deletePost if it doesn't match this:
    override suspend fun deletePost(postId: String): NetworkResult<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return NetworkResult.Error("Not authenticated")

            // Get post to verify ownership and get image URL
            val postDoc = firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .get()
                .await()

            val post = postDoc.toObject(Post::class.java)
                ?: return NetworkResult.Error("Post not found")

            // Verify user owns the post
            if (post.userId != currentUserId) {
                return NetworkResult.Error("You don't have permission to delete this post")
            }

            // Delete image from Firebase Storage
            if (post.imageUrl.isNotEmpty()) {
                try {
                    val imageRef = firebaseStorage.getReferenceFromUrl(post.imageUrl)
                    imageRef.delete().await()
                } catch (e: Exception) {
                    Log.e("PostRepository", "Failed to delete image: ${e.message}")
                }
            }

            // Delete all comments associated with this post
            val commentsSnapshot = firestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val batch = firestore.batch()
            commentsSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Delete post document
            batch.delete(firestore.collection(Constants.POSTS_COLLECTION).document(postId))

            // Update user's posts count
            batch.update(
                firestore.collection(Constants.USERS_COLLECTION).document(currentUserId),
                "postsCount",
                FieldValue.increment(-1)
            )

            batch.commit().await()

            // Delete from local database
            postDao.deletePostById(postId)

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to delete post")
        }
    }

}
