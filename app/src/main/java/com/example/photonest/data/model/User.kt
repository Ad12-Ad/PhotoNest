package com.example.photonest.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val bookmarks: List<String> = emptyList()
)