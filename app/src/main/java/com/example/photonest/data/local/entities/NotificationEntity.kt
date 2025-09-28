package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val fromUserId: String,
    val fromUsername: String,
    val fromUserImage: String,
    val type: String, // Store as String instead of enum
    val postId: String?,
    val commentId: String?,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isClicked: Boolean
)