package com.example.photonest.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val profilePicture: String = "",
    val bio: String = "",
    val website: String = "",
    val birthday: String = "",
    val location: String = "",
    val onboardingCompleted: Boolean = false,
    val joinedDate: Long = System.currentTimeMillis(),
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
)