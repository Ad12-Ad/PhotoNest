package com.example.photonest.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val bookmarkedBy: List<String> = emptyList()
)
