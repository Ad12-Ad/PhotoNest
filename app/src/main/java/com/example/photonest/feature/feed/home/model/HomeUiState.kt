package com.example.photonest.feature.feed.home.model

import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.User

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),

    // Likes bottom sheet
    val isLikesSheetVisible: Boolean = false,
    val likesPostId: String? = null,
    val likedUsers: List<User> = emptyList(),
    val isLikesLoading: Boolean = false,

    val error: String? = null,
    val suggestedUsers: List<User> = emptyList(),
    val trendingPosts: List<Post> = emptyList()
)