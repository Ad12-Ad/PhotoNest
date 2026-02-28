package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.domain.model.Notification
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.INotificationRepository
import com.example.photonest.domain.repository.IUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ToggleFollowUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val postDao: PostDao,
    private val notificationRepository: INotificationRepository // 1. Inject Notification Repo
) {
    /**
     * @param targetUserId The ID of the user to follow/unfollow
     * @param isCurrentlyFollowing The current follow state (used to determine the action)
     * @return NetworkResult indicating success or failure
     */
    suspend operator fun invoke(targetUserId: String, isCurrentlyFollowing: Boolean): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return@withContext NetworkResult.Error("Not authenticated")

        if (currentUserId == targetUserId) {
            return@withContext NetworkResult.Error("Cannot follow yourself")
        }

        val result = if (isCurrentlyFollowing) {
            userRepository.unfollowUser(targetUserId)
        } else {
            userRepository.followUser(targetUserId)
        }

        // Centralized Business Rules
        if (result is NetworkResult.Success) {
            if (isCurrentlyFollowing) {
                // Rule 1: Purge local feed cache on unfollow
                postDao.deletePostsByUser(targetUserId)
            } else {
                // Rule 2: Fire notification on NEW follow
                // 2. Fetch current user details so the notification knows who did the following
                val currentUserResult = userRepository.getUserById(currentUserId)
                val currentUser = currentUserResult.data

                // 3. Construct and fire the notification
                val notification = Notification(
                    id = UUID.randomUUID().toString(),
                    userId = targetUserId, // The person receiving the notification
                    fromUserId = currentUserId,
                    fromUsername = currentUser?.username ?: "Someone",
                    fromUserImage = currentUser?.profilePicture ?: "",
                    type = "FOLLOW",
                    postId = "", // No specific post associated with a follow
                    message = "started following you",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )

                // Fire and forget - if this fails, we don't want to crash the follow action
                notificationRepository.createNotification(notification)
            }
        }

        return@withContext result
    }
}