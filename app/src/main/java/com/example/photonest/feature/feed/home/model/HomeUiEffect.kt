package com.example.photonest.feature.feed.home.model

sealed interface HomeUiEffect {
    data class NavigateToPost(val postId: String) : HomeUiEffect
    data class NavigateToUser(val userId: String) : HomeUiEffect
    data class Share(val text: String) : HomeUiEffect
    data class ShowError(val message: String) : HomeUiEffect
}