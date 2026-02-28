package com.example.photonest.data.repository

import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.core.utils.retryCall
import com.example.photonest.data.local.dao.CommentDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toComment
import com.example.photonest.domain.model.Comment
import com.example.photonest.domain.repository.ICommentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ICommentRepository {

    override suspend fun getCommentsForPost(postId: String): NetworkResult<List<Comment>> {
        val remoteResult = safeFirebaseCall {
            val query = retryCall {
                firestore.collection(Constants.COMMENTS_COLLECTION)
                    .whereEqualTo("postId", postId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
            }

            val currentUserId = firebaseAuth.currentUser?.uid

            val comments = query.documents.mapNotNull { doc ->
                val comment = doc.toObject(Comment::class.java)
                val legacyImage = doc.getString("userProfilePicture") ?: ""
                val finalImage = if (comment?.userImage.isNullOrEmpty()) legacyImage else comment?.userImage ?: ""

                // NEW: Calculate if the current user has liked this comment
                val isLikedByMe = comment?.likedBy?.contains(currentUserId) == true

                comment?.copy(
                    id = doc.id,
                    userImage = finalImage,
                    isLiked = isLikedByMe // Map the boolean for the UI
                )
            }.filter { it.parentCommentId == null }

            commentDao.insertComments(comments.map { it.toEntity() })
            comments
        }

        return when(remoteResult) {
            is NetworkResult.Success -> remoteResult
            else -> {
                val localComments = commentDao.getCommentsForPost(postId).map { it.toComment() }
                NetworkResult.Success(localComments)
            }
        }
    }

    override suspend fun likeComment(commentId: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")

        firestore.collection(Constants.COMMENTS_COLLECTION).document(commentId).update(
            "likeCount", FieldValue.increment(1),
            "likedBy", FieldValue.arrayUnion(currentUserId) // Track WHO liked it
        ).await()
    }

    override suspend fun unlikeComment(commentId: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")

        firestore.collection(Constants.COMMENTS_COLLECTION).document(commentId).update(
            "likeCount", FieldValue.increment(-1),
            "likedBy", FieldValue.arrayRemove(currentUserId) // Remove them from the list
        ).await()
    }
    override suspend fun addComment(comment: Comment): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
        val commentId = UUID.randomUUID().toString()

        val commentData = hashMapOf(
            "id" to commentId,
            "postId" to comment.postId,
            "userId" to currentUserId,
            "userName" to comment.userName,
            "userImage" to comment.userImage, // FIXED: Matches Comment.kt mapping exactly
            "text" to comment.text,
            "timestamp" to System.currentTimeMillis(),
            "likeCount" to 0,
            "parentCommentId" to comment.parentCommentId
        )

        firestore.collection(Constants.COMMENTS_COLLECTION)
            .document(commentId)
            .set(commentData)
            .await()

        firestore.collection(Constants.POSTS_COLLECTION)
            .document(comment.postId)
            .update("commentCount", FieldValue.increment(1))
            .await()

        val newComment = comment.copy(
            id = commentId,
            userId = currentUserId,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insertComment(newComment.toEntity())
    }

    override suspend fun deleteComment(commentId: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")

        val commentDoc = firestore.collection(Constants.COMMENTS_COLLECTION).document(commentId).get().await()
        val comment = commentDoc.toObject(Comment::class.java) ?: throw IllegalStateException("Comment not found")

        if (comment.userId != currentUserId) throw IllegalStateException("You don't have permission to delete this comment")

        firestore.collection(Constants.COMMENTS_COLLECTION).document(commentId).delete().await()
        firestore.collection(Constants.POSTS_COLLECTION).document(comment.postId).update("commentCount", FieldValue.increment(-1)).await()

        commentDao.deleteCommentById(commentId)
    }

    override suspend fun getRepliesForComment(commentId: String): NetworkResult<List<Comment>> {
        val remoteResult = safeFirebaseCall {
            val query = firestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("parentCommentId", commentId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                val comment = doc.toObject(Comment::class.java)
                val legacyImage = doc.getString("userProfilePicture") ?: ""
                val finalImage = if (comment?.userImage.isNullOrEmpty()) legacyImage else comment?.userImage ?: ""

                comment?.copy(id = doc.id, userImage = finalImage)
            }
        }

        return when(remoteResult) {
            is NetworkResult.Success -> remoteResult
            else -> {
                val localReplies = commentDao.getRepliesForComment(commentId).map { it.toComment() }
                NetworkResult.Success(localReplies)
            }
        }
    }

    override suspend fun reportComment(commentId: String, reason: String): NetworkResult<Unit> = safeFirebaseCall {
        val currentUserId = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")

        val reportData = mapOf(
            "commentId" to commentId,
            "reporterId" to currentUserId,
            "reason" to reason,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection(Constants.REPORTS_COLLECTION).add(reportData).await()
    }
}