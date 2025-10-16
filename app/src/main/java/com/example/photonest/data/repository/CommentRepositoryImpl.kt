package com.example.photonest.data.repository

import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.local.dao.CommentDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.mapper.toComment
import com.example.photonest.data.model.Comment
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

    override suspend fun getCommentsForPost(postId: String): Resource<List<Comment>> {
        return try {
            val query = firestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("postId", postId)
                .whereEqualTo("parentCommentId", null)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val comments = query.documents.mapNotNull { doc ->
                val comment = doc.toObject(Comment::class.java)?.copy(id = doc.id)
                comment
            }.map { comment ->
                // Fetch user info for each comment userId
                val userSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                    .document(comment.userId)
                    .get()
                    .await()

                val userName = userSnapshot.getString("name") ?: "Anonymous"
                val userImage = userSnapshot.getString("profilePicture") ?: ""

                comment.copy(
                    userName = userName,
                    userImage = userImage
                )
            }

            // Cache locally
            commentDao.insertComments(comments.map { it.toEntity() })

            Resource.Success(comments)
        } catch (e: Exception) {
            val localComments = commentDao.getCommentsForPost(postId).map { it.toComment() }
            Resource.Success(localComments)
        }
    }


    override suspend fun addComment(comment: Comment): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")
            val commentId = UUID.randomUUID().toString()

            // Create comment data as Map to ensure userId field exists
            val commentData = hashMapOf(
                "id" to commentId,
                "postId" to comment.postId,
                "userId" to currentUserId,
                "userName" to comment.userName,
                "userProfilePicture" to comment.userImage,
                "text" to comment.text,
                "timestamp" to System.currentTimeMillis(),
                "likeCount" to 0,
                "parentCommentId" to comment.parentCommentId
            )

            // Add to Firestore
            firestore.collection(Constants.COMMENTS_COLLECTION)
                .document(commentId)
                .set(commentData)
                .await()

            // Update post comment count
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

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    override suspend fun deleteComment(commentId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.COMMENTS_COLLECTION)
                .document(commentId)
                .delete()
                .await()

            commentDao.deleteCommentById(commentId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete comment")
        }
    }

    override suspend fun likeComment(commentId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.COMMENTS_COLLECTION)
                .document(commentId)
                .update("likeCount", FieldValue.increment(1))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like comment")
        }
    }

    override suspend fun unlikeComment(commentId: String): Resource<Unit> {
        return try {
            firestore.collection(Constants.COMMENTS_COLLECTION)
                .document(commentId)
                .update("likeCount", FieldValue.increment(-1))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike comment")
        }
    }

    override suspend fun getRepliesForComment(commentId: String): Resource<List<Comment>> {
        return try {
            val query = firestore.collection(Constants.COMMENTS_COLLECTION)
                .whereEqualTo("parentCommentId", commentId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val replies = query.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(id = doc.id)
            }

            Resource.Success(replies)
        } catch (e: Exception) {
            val localReplies = commentDao.getRepliesForComment(commentId).map { it.toComment() }
            Resource.Success(localReplies)
        }
    }

    override suspend fun reportComment(commentId: String, reason: String): Resource<Unit> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Error("Not authenticated")

            val reportData = mapOf(
                "commentId" to commentId,
                "reporterId" to currentUserId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection(Constants.REPORTS_COLLECTION)
                .add(reportData)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to report comment")
        }
    }
}
