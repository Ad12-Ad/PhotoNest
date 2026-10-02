package com.example.photonest.data.repository

import android.content.Context
import android.util.Log
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.retryCall
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.CommentDao
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.local.entities.TrendingFeedEntity
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.PostDetail
import com.example.photonest.domain.model.User
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
    private val followDao: FollowDao,
    private val commentDao: CommentDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : IPostRepository {

    override fun getPosts(): Flow<NetworkResult<List<Post>>> = flow {
        emit(NetworkResult.Loading())

        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(NetworkResult.Error("Not authenticated"))
            return@flow
        }

        // 1. THE FIX: Only grab local posts for people you ACTUALLY follow
        val localFollowingIds = followDao.getFollowingIds(currentUserId).toMutableList()
        localFollowingIds.add(currentUserId) // Always include own posts

        val localPosts = postDao.getPostsByUsers(localFollowingIds).map { it.toPost() }

        // 2. ALWAYS emit the local state first.
        // If it's empty, it instantly clears the ghost feed and shows your EmptyState UI!
        emit(NetworkResult.Success(localPosts))

        val remoteResult = safeFirebaseCall {
            // 3. Fetch fresh follows from Firebase
            val followsSnapshot = retryCall {
                firestore.collection(Constants.FOLLOWS_COLLECTION)
                    .whereEqualTo("followerId", currentUserId)
                    .get().await()
            }

            val remoteFollowingIds = followsSnapshot.documents
                .mapNotNull { it.getString("followingId") }

            // 4. CRITICAL: Sync these follows to the local database so offline mode works!
            remoteFollowingIds.forEach { followingId ->
                followDao.insertFollow(
                    com.example.photonest.data.local.entities.FollowEntity(
                        id = "${currentUserId}_${followingId}",
                        followerId = currentUserId,
                        followingId = followingId,
                        timestamp = System.currentTimeMillis(),
                        isAccepted = true
                    )
                )
            }

            val searchIds = remoteFollowingIds.toMutableList().apply { add(currentUserId) }

            // 5. Fetch the posts for these specific users
            val allPosts = mutableListOf<Post>()
            searchIds.chunked(10).forEach { batch ->
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

            val followedUserSet = remoteFollowingIds.toSet()

            val enrichedPosts = sortedPosts.map { post ->
                post.copy(
                    isLiked = likedPostIds.contains(post.id),
                    isBookmarked = bookmarkedPostIds.contains(post.id),
                    isUserFollowed = post.userId != currentUserId && followedUserSet.contains(post.userId)
                )
            }

            // 6. Update local cache with the fetched posts
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

        return safeFirebaseCall {
            firestore.collection(Constants.BOOKMARKS_COLLECTION).document("${userId}_$postId")
                .set(mapOf("userId" to userId, "postId" to postId, "timestamp" to System.currentTimeMillis())).await()
            postDao.updatePostBookmark(postId, true)
        }
    }

    override suspend fun unbookmarkPost(postId: String): NetworkResult<Unit> {
        val userId = firebaseAuth.currentUser?.uid ?: return NetworkResult.Error("Not authenticated")

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
            val query = firestore.collection(Constants.POSTS_COLLECTION)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(20).get().await()

            val posts = query.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            // 1. Save full posts to the single source of truth
            postDao.insertPosts(posts.map { it.toEntity() })

            // 2. Clear old mapping and save new mapping
            postDao.clearTrendingFeed()
            postDao.insertTrendingFeed(posts.map { TrendingFeedEntity(it.id) })

            posts
        }

        return when (result) {
            is NetworkResult.Success -> result
            else -> {
                // Read strictly from the mapping table!
                val localTrending = postDao.getTrendingFeedPosts().map { it.toPost() }
                NetworkResult.Success(localTrending)
            }
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

        // 1. Fetch the post to verify ownership and get image URL
        val postDoc = firestore.collection(Constants.POSTS_COLLECTION).document(postId).get().await()
        val post = postDoc.toObject(Post::class.java) ?: throw IllegalStateException("Post not found")

        if (post.userId != currentUserId) throw IllegalStateException("You don't have permission to delete this post")

        // 2. Delete the Image from Storage first
        if (post.imageUrl.isNotEmpty() && !post.imageUrl.contains("picsum.photos")) {
            try {
                firebaseStorage.getReferenceFromUrl(post.imageUrl).delete().await()
            } catch (e: Exception) {
                Log.e("PostRepository", "Storage delete failed: ${e.message}")
            }
        }

        // 3. Execute Deletion Batch
        val batch = firestore.batch()

        // Delete the post itself
        batch.delete(firestore.collection(Constants.POSTS_COLLECTION).document(postId))

        // Update user post count
        batch.update(
            firestore.collection(Constants.USERS_COLLECTION).document(currentUserId),
            "postsCount",
            FieldValue.increment(-1)
        )

        batch.commit().await()

        // 4. Local Cleanup
        postDao.deletePostById(postId)
        commentDao.deleteCommentsForPost(postId)
    }
}