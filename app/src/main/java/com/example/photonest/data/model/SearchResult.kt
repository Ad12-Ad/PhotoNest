package com.example.photonest.data.model

data class SearchResult(
    val users: List<User> = emptyList(),
    val posts: List<Post> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalResults: Int = 0,
    val query: String = ""
)