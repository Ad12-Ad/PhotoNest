package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Long,
    val category: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val location: String,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val likedBy: List<String>,
    val tags: List<String>,
    val aspectRatio: Float,
    val isEdited: Boolean,
    val editedAt: Long?
)