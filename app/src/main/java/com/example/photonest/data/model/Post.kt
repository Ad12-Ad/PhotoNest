package com.example.photonest.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val location: String = "",
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val likedBy: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val aspectRatio: Float = 1f,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val isUserFollowed: Boolean = false
)
