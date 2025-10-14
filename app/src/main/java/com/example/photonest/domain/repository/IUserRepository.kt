package com.example.photonest.domain.repository

import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getCurrentUser(): Flow<Resource<User?>>
    suspend fun getUserById(userId: String): Resource<User?>
    suspend fun getUserByUsername(username: String): Resource<User?>
    suspend fun updateUser(user: User): Resource<Unit>
    suspend fun searchUsers(query: String): Resource<List<User>>
    suspend fun followUser(userId: String): Resource<Unit>
    suspend fun unfollowUser(userId: String): Resource<Unit>
    suspend fun getFollowers(userId: String): Resource<List<User>>
    suspend fun getFollowing(userId: String): Resource<List<User>>
    suspend fun getUserProfile(userId: String): Resource<UserProfile>
    suspend fun uploadProfilePicture(imageUri: String): Resource<String>
    suspend fun getPopularUsers(): Resource<List<User>>
    suspend fun blockUser(userId: String): Resource<Unit>
    suspend fun unblockUser(userId: String): Resource<Unit>
    suspend fun getLikedPostIdsByUserId(userId: String): List<String>
}
