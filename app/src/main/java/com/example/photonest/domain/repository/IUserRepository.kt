package com.example.photonest.domain.repository

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getCurrentUser(): Flow<NetworkResult<User?>>
    suspend fun getUserById(userId: String): NetworkResult<User?>
    suspend fun getUserByUsername(username: String): NetworkResult<User?>
    suspend fun updateUser(user: User): NetworkResult<Unit>
    suspend fun searchUsers(query: String): NetworkResult<List<User>>
    suspend fun followUser(userId: String): NetworkResult<Unit>
    suspend fun unfollowUser(userId: String): NetworkResult<Unit>
    suspend fun getFollowers(userId: String): NetworkResult<List<User>>
    suspend fun getFollowing(userId: String): NetworkResult<List<User>>
    suspend fun getUserProfile(userId: String): NetworkResult<UserProfile>
    suspend fun uploadProfilePicture(imageUri: String): NetworkResult<String>
    suspend fun getPopularUsers(): NetworkResult<List<User>>
    suspend fun blockUser(userId: String): NetworkResult<Unit>
    suspend fun unblockUser(userId: String): NetworkResult<Unit>
    suspend fun getLikedPostIdsByUserId(userId: String): List<String>
    suspend fun getBookmarkedPostIdsByUserId(userId: String): List<String>
    suspend fun isFollowing(userId: String): NetworkResult<Boolean>
}
