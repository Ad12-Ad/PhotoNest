package com.example.photonest.data.repository

import android.util.Log
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toPost
import com.example.photonest.data.mapper.toUser
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.PostDetail
import com.example.photonest.data.model.User
import com.example.photonest.domain.repository.IPostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
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
    private val firebaseStorage: FirebaseStorage
) : IPostRepository {

    override fun getPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = firebaseAuth.currentUser?.uid

            if (currentUserId == null) {
                emit(Resource.Error("Not authenticated"))
                return@flow
            }

            val followsQuery = firestore.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .get()
                .await()

            val followedUserIds = followsQuery.documents
                .mapNotNull { it.getString("followingId") }
                .toMutableList()

            followedUserIds.add(currentUserId)

            if (followedUserIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val allPosts = mutableListOf<Post>()
            val batches = followedUserIds.chunked(10)


            for (batch in batches) {
                val postsQuery = firestore.collection(Constants.POSTS_COLLECTION)
                    .whereIn("userId", batch)
                    .limit(50)
                    .get()
                    .await()

                val batchPosts = postsQuery.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }

                allPosts.addAll(batchPosts)
            }

            val sortedPosts = allPosts.sortedByDescending { it.timestamp }

            val enrichedPosts = if (sortedPosts.isNotEmpty()) {
                // Get bookmarks
                val bookmarksQuery = firestore.collection("bookmarks")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val bookmarkedPostIds = bookmarksQuery.documents
                    .mapNotNull { it.getString("postId") }
                    .toSet()

                val followedUserIdsSet = followedUserIds.toSet()

                // Enrich posts
                sortedPosts.map { post ->
                    post.copy(
                        isLiked = post.likedBy.contains(currentUserId),
                        isBookmarked = bookmarkedPostIds.contains(post.id),
                        isUserFollowed = followedUserIdsSet.contains(post.userId)
                    )
                }
            } else {
                sortedPosts
            }

            // Save to local database
            enrichedPosts.forEach { post ->
                postDao.insertPost(post.toEntity())
            }

            emit(Resource.Success(enrichedPosts))

//            val postsQuery = firestore.collection(Constants.POSTS_COLLECTION)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(50)
//                .get()
//                .await()
//
//            val posts = postsQuery.documents.mapNotNull { doc ->
//                doc.toObject(Post::class.java)?.copy(id = doc.id)
//            }
//
//            if (currentUserId != null && posts.isNotEmpty()) {
//                val followsQuery = firestore.collection("follows")
//                    .whereEqualTo("followerId", currentUserId)
//                    .get()
//                    .await()
//
//                val followedUserIds = followsQuery.documents
//                    .mapNotNull { it.getString("followingId") }
//                    .toSet()
//
//                val bookmarksQuery = firestore.collection("bookmarks")
//                    .whereEqualTo("userId", currentUserId)
//                    .get()
//                    .await()
//
//                val bookmarkedPostIds = bookmarksQuery.documents
//                    .mapNotNull { it.getString("postId") }
//                    .toSet()
//
//                val enrichedPosts = posts.map { post ->
//                    post.copy(
//                        isLiked = post.likedBy.contains(currentUserId),
//                        isBookmarked = bookmarkedPostIds.contains(post.id),
//                        isUserFollowed = followedUserIds.contains(post.userId)
//                    )
//                }
//
//                enrichedPosts.forEach { post ->
//                    postDao.insertPost(post.toEntity())
//                }
//
//                emit(Resource.Success(enrichedPosts))
//            } else {
//                emit(Resource.Success(posts))
//            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Failed to get posts: ${e.message}")
            val localPosts = postDao.getAllPosts().map { it.toPost() }
            emit(Resource.Success(localPosts))
        }
    }

    private suspend fun checkIfBookmarked(userId: String, postId: String): Boolean {
        return try {
            val bookmarkQuery = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            !bookmarkQuery.isEmpty
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun getPostById(postId: String): Resource<PostDetail?> {
        return try {
            val postDoc = firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .get()
                .await()

            val post = postDoc.toObject(Post::class.java)?.copy(id = postDoc.id)

            if (post != null) {
                // Get user details
                val user = userDao.getUserById(post.userId)?.toUser()

                val postDetail = PostDetail(
                    post = post,
                    user = user,
                    isOwner = firebaseAuth.currentUser?.uid == post.userId
                )

                Resource.Success(postDetail)
            } else {
                Resource.Error("Post not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get post")
        }
    }

    override suspend fun getUserPosts(userId: String): Resource<List<Post>> {
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
            Resource.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getPostsByUser(userId).map { it.toPost() }
            Resource.Success(localPosts)
        }
    }

    override suspend fun createPost(post: Post, imageUri: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            Log.d("PostRepository", "Creating post with imageUri: $imageUri")

            // Upload image to Firebase Storage if imageUri is provided
            val imageUrl = if (imageUri.isNotEmpty() && imageUri.startsWith("content://")) {
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
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("PostRepository", "Failed to create post: ${e.message}")
            Resource.Error(e.message ?: "Failed to create post")
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


    override suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            // Delete from Firestore
            firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .delete()
                .await()

            // Delete from local database
            postDao.deletePostById(postId)

            // Update user's post count
            firestore.collection(Constants.USERS_COLLECTION)
                .document(currentUserId)
                .update("postsCount", FieldValue.increment(-1))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }

    override suspend fun likePost(postId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            // Check if like already exists
            val existingLikeQuery = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (existingLikeQuery.isEmpty) {
                // Create unique like ID
                val likeId = "${currentUserId}_${postId}"

                val likeData = hashMapOf(
                    "userId" to currentUserId,
                    "postId" to postId,
                    "timestamp" to System.currentTimeMillis()
                )

                // Use .set() with specific document ID instead of .add()
                firestore.collection(Constants.LIKES_COLLECTION)
                    .document(likeId)
                    .set(likeData)
                    .await()

                // Update post like count
                firestore.collection(Constants.POSTS_COLLECTION)
                    .document(postId)
                    .update(
                        "likeCount", FieldValue.increment(1),
                        "likedBy", FieldValue.arrayUnion(currentUserId)
                    )
                    .await()

                try {
                    val postDoc = firestore.collection(Constants.POSTS_COLLECTION)
                        .document(postId)
                        .get()
                        .await()

                    val post = postDoc.toObject(Post::class.java)
                    val postOwnerId = post?.userId

                    if (postOwnerId != null && postOwnerId != currentUserId) {
                        val currentUserDoc = firestore.collection(Constants.USERS_COLLECTION)
                            .document(currentUserId)
                            .get()
                            .await()

                        val currentUser = currentUserDoc.toObject(User::class.java)

                        val notificationData = hashMapOf(
                            "userId" to postOwnerId,
                            "fromUserId" to currentUserId,
                            "fromUsername" to (currentUser?.username ?: "Someone"),
                            "fromUserImage" to (currentUser?.profilePicture ?: ""),
                            "type" to "LIKE",
                            "postId" to postId,
                            "message" to "${currentUser?.username ?: "Someone"} liked your post",
                            "timestamp" to System.currentTimeMillis(),
                            "isRead" to false,
                            "isClicked" to false
                        )

                        firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
                            .add(notificationData)
                            .await()

                        Log.d("PostRepository", "Notification created for like")
                    }
                } catch (e: Exception) {
                    Log.e("PostRepository", "Failed to create notification: ${e.message}")
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like post")
        }
    }

    override suspend fun unlikePost(postId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            // Remove like from Firestore
            val likeQuery = firestore.collection(Constants.LIKES_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            likeQuery.documents.forEach { doc ->
                doc.reference.delete()
            }

            // Update post like count
            firestore.collection(Constants.POSTS_COLLECTION)
                .document(postId)
                .update(
                    "likeCount", FieldValue.increment(-1),
                    "likedBy", FieldValue.arrayRemove(currentUserId)
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike post")
        }
    }

    override suspend fun bookmarkPost(postId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return Resource.Error("Not authenticated")

            val bookmarkId = "${currentUserId}_${postId}"

            // Check if already bookmarked
            val existingBookmark = firestore.collection("bookmarks")
                .document(bookmarkId)
                .get()
                .await()

            if (existingBookmark.exists()) {
                return Resource.Error("Post already bookmarked")
            }

            val bookmarkData = hashMapOf(
                "userId" to currentUserId,
                "postId" to postId,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("bookmarks")
                .document(bookmarkId)
                .set(bookmarkData)
                .await()

            postDao.updatePostBookmark(postId, true)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to bookmark post")
        }
    }

    override suspend fun unbookmarkPost(postId: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return Resource.Error("Not authenticated")

            // ✅ DELETE SPECIFIC BOOKMARK DOCUMENT
            val bookmarkId = "${currentUserId}_${postId}"

            firestore.collection("bookmarks")
                .document(bookmarkId)
                .delete()
                .await()

            // ✅ UPDATE LOCAL DATABASE
            postDao.updatePostBookmark(postId, false)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unbookmark post")
        }
    }

    override suspend fun getBookmarkedPosts(): Resource<List<Post>> {
        return try {
            val posts = postDao.getBookmarkedPosts().map { it.toPost() }
            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get bookmarked posts")
        }
    }

    override suspend fun getTrendingPosts(): Resource<List<Post>> {
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
            Resource.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getTrendingPosts(20).map { it.toPost() }
            Resource.Success(localPosts)
        }
    }

    override suspend fun getPostsByCategory(category: String): Resource<List<Post>> {
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
            Resource.Success(posts)
        } catch (e: Exception) {
            val localPosts = postDao.getPostsByCategory(category).map { it.toPost() }
            if (localPosts.isNotEmpty()) {
                Resource.Success(localPosts)
            } else {
                Resource.Error(e.message ?: "Failed to load category posts")
            }
        }
    }

    override suspend fun searchPosts(query: String): Resource<List<Post>> {
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

            Resource.Success(filteredPosts)
        } catch (e: Exception) {
            val localPosts = postDao.searchPosts(query).map { it.toPost() }
            Resource.Success(localPosts)
        }
    }

    override suspend fun reportPost(postId: String, reason: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

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

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to report post")
        }
    }
    override suspend fun getPostsByIds(postIds: List<String>): Resource<List<Post>> {
        return try {
            if (postIds.isEmpty()) {
                return Resource.Success(emptyList())
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

            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get posts by IDs")
        }
    }

}
