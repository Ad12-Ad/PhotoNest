package com.example.photonest.data.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val parentCommentId: String? = null, // null for top-level comments
    val isLiked: Boolean = false,
    val isOwner: Boolean = false,
    val likedBy: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null
)
