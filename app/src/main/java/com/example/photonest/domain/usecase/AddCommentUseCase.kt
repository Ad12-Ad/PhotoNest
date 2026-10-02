package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Comment
import com.example.photonest.domain.model.Notification
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.domain.repository.INotificationRepository
import com.example.photonest.domain.repository.IUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val commentRepository: ICommentRepository,
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository,
    private val notificationRepository: INotificationRepository
) {
    suspend operator fun invoke(postId: String, postOwnerId: String, text: String): NetworkResult<Comment> = withContext(Dispatchers.IO) {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return@withContext NetworkResult.Error("Not authenticated")

        val currentUser = userRepository.getUserById(currentUserId).data
            ?: return@withContext NetworkResult.Error("User data not found")

        val comment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = currentUserId,
            userName = currentUser.username,
            userImage = currentUser.profilePicture,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        val result = commentRepository.addComment(comment)

        // Centralized Business Rule: Notify post owner
        if (result is NetworkResult.Success && postOwnerId != currentUserId) {
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                userId = postOwnerId,
                fromUserId = currentUserId,
                fromUsername = currentUser.username,
                fromUserImage = currentUser.profilePicture,
                type = "COMMENT",
                postId = postId,
                message = "commented on your post",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            notificationRepository.createNotification(notification)
        }

        return@withContext if (result is NetworkResult.Success) {
            NetworkResult.Success(comment)
        } else {
            NetworkResult.Error(result.message ?: "Failed to add comment")
        }
    }
}