package com.example.photonest.feature.feed.home.model

sealed interface HomeUiEvent {

    data object Load : HomeUiEvent
    data object Refresh : HomeUiEvent

    data class PostClicked(val postId: String) : HomeUiEvent
    data class UserClicked(val userId: String) : HomeUiEvent

    data class ToggleLike(val postId: String) : HomeUiEvent
    data class ToggleBookmark(val postId: String) : HomeUiEvent
    data class ToggleFollow(val userId: String, val postId: String) : HomeUiEvent

    data class OpenLikes(val postId: String) : HomeUiEvent
    data object CloseLikes : HomeUiEvent

    data class SharePost(val postId: String) : HomeUiEvent
    data object DismissError : HomeUiEvent
}