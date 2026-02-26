package com.example.photonest.domain.repository

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Comment

interface ICommentRepository {
    suspend fun getCommentsForPost(postId: String): NetworkResult<List<Comment>>
    suspend fun addComment(comment: Comment): NetworkResult<Unit>
    suspend fun deleteComment(commentId: String): NetworkResult<Unit>
    suspend fun likeComment(commentId: String): NetworkResult<Unit>
    suspend fun unlikeComment(commentId: String): NetworkResult<Unit>
    suspend fun getRepliesForComment(commentId: String): NetworkResult<List<Comment>>
    suspend fun reportComment(commentId: String, reason: String): NetworkResult<Unit>
}
