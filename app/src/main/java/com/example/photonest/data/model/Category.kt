package com.example.photonest.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val color: String = "#FF6B6B",
    val postsCount: Int = 0,
    val isPopular: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
