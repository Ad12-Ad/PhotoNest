package com.example.photonest.domain.repository

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.PostDetail
import com.example.photonest.data.model.User
import kotlinx.coroutines.flow.Flow

interface IPostRepository {
    fun getPosts(): Flow<NetworkResult<List<Post>>>
    suspend fun getPostById(postId: String): NetworkResult<PostDetail?>
    suspend fun getUserPosts(userId: String): NetworkResult<List<Post>>
    suspend fun createPost(post: Post, imageUri: String): NetworkResult<Unit>
    suspend fun deletePost(postId: String): NetworkResult<Unit>
    suspend fun likePost(postId: String): NetworkResult<Unit>
    suspend fun unlikePost(postId: String): NetworkResult<Unit>
    suspend fun bookmarkPost(postId: String): NetworkResult<Unit>
    suspend fun unbookmarkPost(postId: String): NetworkResult<Unit>
    suspend fun getBookmarkedPosts(): NetworkResult<List<Post>>
    suspend fun getTrendingPosts(): NetworkResult<List<Post>>
    suspend fun getPostsByCategory(category: String): NetworkResult<List<Post>>
    suspend fun searchPosts(query: String): NetworkResult<List<Post>>
    suspend fun reportPost(postId: String, reason: String): NetworkResult<Unit>
    suspend fun getPostsByIds(postIds: List<String>): NetworkResult<List<Post>>
    suspend fun getUsersWhoLikedPost(postId: String): NetworkResult<List<User>>
}
