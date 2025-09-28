package com.example.photonest.data.model

data class Follow(
    val id: String = "",
    val followerId: String = "", // User who is following
    val followingId: String = "", // User being followed
    val timestamp: Long = System.currentTimeMillis(),
    val isAccepted: Boolean = true // For private accounts
)
