package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.CommentEntity

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE postId = :postId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    suspend fun getCommentsForPost(postId: String): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE parentCommentId = :commentId ORDER BY timestamp ASC")
    suspend fun getRepliesForComment(commentId: String): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): CommentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsForPost(postId: String)

    @Query("UPDATE comments SET likeCount = likeCount + 1, isLiked = 1 WHERE id = :commentId")
    suspend fun likeComment(commentId: String)

    @Query("UPDATE comments SET likeCount = likeCount - 1, isLiked = 0 WHERE id = :commentId")
    suspend fun unlikeComment(commentId: String)

    @Query("UPDATE comments SET replyCount = replyCount + 1 WHERE id = :commentId")
    suspend fun incrementReplyCount(commentId: String)

    @Query("UPDATE comments SET replyCount = replyCount - 1 WHERE id = :commentId AND replyCount > 0")
    suspend fun decrementReplyCount(commentId: String)

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountForPost(postId: String): Int

    @Query("DELETE FROM comments")
    suspend fun clearAllComments()
}