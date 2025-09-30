package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val text: String,
    val timestamp: Long,
    val likeCount: Int,
    val replyCount: Int,
    val parentCommentId: String?,
    val isLiked: Boolean,
    val isOwner: Boolean,
    val likedBy: String, // JSON string of list
    val mentions: String, // JSON string of list
    val isEdited: Boolean,
    val editedAt: Long?
)
