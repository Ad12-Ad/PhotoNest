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
    val parentCommentId: String? = null,
    val replies: List<Comment> = emptyList(),
    val mentions: List<String> = emptyList(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null
)
