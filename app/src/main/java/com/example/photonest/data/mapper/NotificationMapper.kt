package com.example.photonest.data.mapper

import com.example.photonest.data.local.entities.NotificationEntity
import com.example.photonest.data.model.Notification
import com.example.photonest.data.model.NotificationType

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        userId = userId,
        fromUserId = fromUserId,
        fromUsername = fromUsername,
        fromUserImage = fromUserImage,
        type = type.name,
        postId = postId,
        commentId = commentId,
        message = message,
        timestamp = timestamp,
        isRead = isRead,
        isClicked = isClicked
    )
}

fun NotificationEntity.toNotification(): Notification {
    return Notification(
        id = id,
        userId = userId,
        fromUserId = fromUserId,
        fromUsername = fromUsername,
        fromUserImage = fromUserImage,
        type = try {
            NotificationType.valueOf(type)
        } catch (e: Exception) {
            NotificationType.LIKE // Default fallback
        },
        postId = postId,
        commentId = commentId,
        message = message,
        timestamp = timestamp,
        isRead = isRead,
        isClicked = isClicked
    )
}