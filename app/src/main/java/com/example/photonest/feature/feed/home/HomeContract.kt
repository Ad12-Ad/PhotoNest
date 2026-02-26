package com.example.photonest.feature.feed.home

import com.example.photonest.data.model.Post
import com.example.photonest.data.model.User

data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),

    // Likes bottom sheet
    val isLikesSheetVisible: Boolean = false,
    val likesPostId: String? = null,
    val likedUsers: List<User> = emptyList(),
    val isLikesLoading: Boolean = false,

    val error: String? = null
)

sealed interface HomeEvent {

    data object Load : HomeEvent
    data object Refresh : HomeEvent

    data class PostClicked(val postId: String) : HomeEvent
    data class UserClicked(val userId: String) : HomeEvent

    data class ToggleLike(val postId: String) : HomeEvent
    data class ToggleBookmark(val postId: String) : HomeEvent
    data class ToggleFollow(val userId: String, val postId: String) : HomeEvent

    data class OpenLikes(val postId: String) : HomeEvent
    data object CloseLikes : HomeEvent

    data class SharePost(val postId: String) : HomeEvent
    data object DismissError : HomeEvent
}

sealed interface HomeEffect {
    data class NavigateToPost(val postId: String) : HomeEffect
    data class NavigateToUser(val userId: String) : HomeEffect
    data class Share(val text: String) : HomeEffect
    data class ShowError(val message: String) : HomeEffect
}