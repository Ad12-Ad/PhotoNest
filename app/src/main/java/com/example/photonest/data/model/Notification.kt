package com.example.photonest.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromUserImage: String = "",
    val type: String = "LIKE",
    val postId: String? = null,
    val commentId: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isClicked: Boolean = false
){
    fun getNotificationType(): NotificationType {
        return try {
            NotificationType.valueOf(type.uppercase())
        } catch (e: Exception) {
            NotificationType.LIKE
        }
    }
}

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    FOLLOW_REQUEST,
    MENTION,
    POST_UPLOAD,
    STORY_UPLOAD
}
