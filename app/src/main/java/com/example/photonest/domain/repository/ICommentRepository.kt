package com.example.photonest.domain.repository

import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Comment

interface ICommentRepository {
    suspend fun getCommentsForPost(postId: String): Resource<List<Comment>>
    suspend fun addComment(comment: Comment): Resource<Unit>
    suspend fun deleteComment(commentId: String): Resource<Unit>
    suspend fun likeComment(commentId: String): Resource<Unit>
    suspend fun unlikeComment(commentId: String): Resource<Unit>
    suspend fun getRepliesForComment(commentId: String): Resource<List<Comment>>
    suspend fun reportComment(commentId: String, reason: String): Resource<Unit>
}
