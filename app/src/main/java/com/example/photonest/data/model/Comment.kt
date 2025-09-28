package com.example.photonest.data.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePicture: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)