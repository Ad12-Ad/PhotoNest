package com.example.photonest.data.model

data class Post(
    val id: String,
    val userName: String,
    val userImage: Int,
    val imageUrl: Int,
    val timestamp: String,
    val category: List<String>,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean
)
