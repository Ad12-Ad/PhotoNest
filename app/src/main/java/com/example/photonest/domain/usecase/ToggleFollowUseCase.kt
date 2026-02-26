package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.IUserRepository
import javax.inject.Inject

class ToggleFollowUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val postDao: PostDao
) {
    /**
     * @param targetUserId The ID of the user to follow/unfollow
     * @param isCurrentlyFollowing The current follow state (used to determine the action)
     * @return NetworkResult indicating success or failure
     */
    suspend operator fun invoke(targetUserId: String, isCurrentlyFollowing: Boolean): NetworkResult<Unit> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return NetworkResult.Error("Not authenticated")

        if (currentUserId == targetUserId) {
            return NetworkResult.Error("Cannot follow yourself")
        }

        val result = if (isCurrentlyFollowing) {
            userRepository.unfollowUser(targetUserId)
        } else {
            userRepository.followUser(targetUserId)
        }

        // Centralized Business Rule:
        // If we successfully unfollowed someone, purge their posts from the local feed cache.
        if (result is NetworkResult.Success && isCurrentlyFollowing) {
            postDao.deletePostsByUser(targetUserId)
        }

        return result
    }
}