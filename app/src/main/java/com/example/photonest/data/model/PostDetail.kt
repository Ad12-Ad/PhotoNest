package com.example.photonest.data.model

data class PostDetail(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val relatedPosts: List<Post> = emptyList(),
    val isOwner: Boolean = false,
    val user: User? = null
)
