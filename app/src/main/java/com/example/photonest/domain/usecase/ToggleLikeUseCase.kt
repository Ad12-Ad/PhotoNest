package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Notification
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.INotificationRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ToggleLikeUseCase @Inject constructor(
    private val postRepository: IPostRepository,
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository,
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(post: Post): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return@withContext NetworkResult.Error("Not authenticated")

        val wasLiked = post.isLiked

        val result = if (wasLiked) {
            postRepository.unlikePost(post.id)
        } else {
            postRepository.likePost(post.id)
        }

        // Centralized Business Rule: Fire notification only on NEW like, and not on own post.
        if (result is NetworkResult.Success && !wasLiked && post.userId != currentUserId) {
            val currentUser = userRepository.getUserById(currentUserId).data

            val notification = Notification(
                id = UUID.randomUUID().toString(),
                userId = post.userId,
                fromUserId = currentUserId,
                fromUsername = currentUser?.username ?: "Someone",
                fromUserImage = currentUser?.profilePicture ?: "",
                type = "LIKE",
                postId = post.id,
                message = "liked your post",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            notificationRepository.createNotification(notification)
        }

        return@withContext result
    }
}