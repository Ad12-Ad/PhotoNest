package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.IUserRepository
import javax.inject.Inject

/**
 * Encapsulates the follow/unfollow business logic.
 * Handles: auth check, current state check, toggle, and error reporting.
 * ViewModels call this and handle optimistic UI updates themselves.
 */
class FollowUserUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository
) {
    /**
     * @param targetUserId The user to follow/unfollow.
     * @return Resource.Success(isNowFollowing) on success, Resource.Error on failure.
     */
    suspend operator fun invoke(targetUserId: String): NetworkResult<Boolean> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return NetworkResult.Error("Not authenticated")

        if (currentUserId == targetUserId) {
            return NetworkResult.Error("Cannot follow yourself")
        }

        val isFollowingResult = userRepository.isFollowing(targetUserId)
        val isCurrentlyFollowing = when (isFollowingResult) {
            is NetworkResult.Success -> isFollowingResult.data == true
            else -> false
        }

        val result = if (isCurrentlyFollowing) {
            userRepository.unfollowUser(targetUserId)
        } else {
            userRepository.followUser(targetUserId)
        }

        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(!isCurrentlyFollowing)
            is NetworkResult.Error -> NetworkResult.Error(
                message = result.message ?: "Unknown error"
            )
            else -> NetworkResult.Error("Unexpected state")
        }
    }
}