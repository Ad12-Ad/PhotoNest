package com.example.photonest.data.model

data class UserProfile(
    val user: User,
    val posts: List<Post> = emptyList(),
    val isFollowing: Boolean = false,
    val isFollowRequestPending: Boolean = false,
    val mutualFollowers: List<User> = emptyList(),
    val mutualFollowersCount: Int = 0,
    val isBlocked: Boolean = false,
    val isCurrentUser: Boolean = false
)
