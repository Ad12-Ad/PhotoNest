package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userProfilePicture: String,
    val content: String,
    val timestamp: Long,
    val likeCount: Int,
    val isLiked: Boolean,
    val parentCommentId: String?,
    val mentions: List<String>,
    val isEdited: Boolean,
    val editedAt: Long?
)
