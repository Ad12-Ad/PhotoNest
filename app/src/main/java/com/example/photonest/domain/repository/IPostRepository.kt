package com.example.photonest.domain.repository

import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.PostDetail
import com.example.photonest.data.model.User
import kotlinx.coroutines.flow.Flow

interface IPostRepository {
    fun getPosts(): Flow<Resource<List<Post>>>
    suspend fun getPostById(postId: String): Resource<PostDetail?>
    suspend fun getUserPosts(userId: String): Resource<List<Post>>
    suspend fun createPost(post: Post, imageUri: String): Resource<Unit>
    suspend fun deletePost(postId: String): Resource<Unit>
    suspend fun likePost(postId: String): Resource<Unit>
    suspend fun unlikePost(postId: String): Resource<Unit>
    suspend fun bookmarkPost(postId: String): Resource<Unit>
    suspend fun unbookmarkPost(postId: String): Resource<Unit>
    suspend fun getBookmarkedPosts(): Resource<List<Post>>
    suspend fun getTrendingPosts(): Resource<List<Post>>
    suspend fun getPostsByCategory(category: String): Resource<List<Post>>
    suspend fun searchPosts(query: String): Resource<List<Post>>
    suspend fun reportPost(postId: String, reason: String): Resource<Unit>
    suspend fun getPostsByIds(postIds: List<String>): Resource<List<Post>>
    suspend fun getUsersWhoLikedPost(postId: String): Resource<List<User>>
}
