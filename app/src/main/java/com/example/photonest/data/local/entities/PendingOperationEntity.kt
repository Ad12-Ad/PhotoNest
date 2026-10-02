package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,          // "LIKE", "UNLIKE", "FOLLOW", "UNFOLLOW", "COMMENT", "BOOKMARK"
    val targetId: String,      // postId, userId, commentId etc.
    val payload: String = "",  // JSON payload for complex operations (e.g., comment text)
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0
)
