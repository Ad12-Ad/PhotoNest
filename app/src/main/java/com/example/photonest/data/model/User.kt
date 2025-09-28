package com.example.photonest.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val profilePicture: String = "",
    val bio: String = "",
    val website: String = "",
    val location: String = "",
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val bookmarks: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis()
)