package com.example.photonest.data.local.dao

import androidx.room.*
import com.example.photonest.data.local.entities.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE postId = :postId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    suspend fun getCommentsForPost(postId: String): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY timestamp ASC")
    suspend fun getRepliesForComment(parentCommentId: String): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: String): CommentEntity?

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getCommentsByUser(userId: String): List<CommentEntity>

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getCommentsForPostPaged(postId: String, limit: Int, offset: Int): List<CommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsForPost(postId: String)

    @Query("UPDATE comments SET isLiked = :isLiked, likeCount = :likeCount WHERE id = :commentId")
    suspend fun updateCommentLike(commentId: String, isLiked: Boolean, likeCount: Int)

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountForPost(postId: String): Int

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun getTotalCommentCount(): Int

    @Query("DELETE FROM comments")
    suspend fun clearAllComments()
}
