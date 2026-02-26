package com.example.photonest.feature.feed.postdetail.model

sealed interface PostDetailEffect {

    data class Share(val text: String) : PostDetailEffect
    data class ShowError(val message: String) : PostDetailEffect
    data object NavigateBack : PostDetailEffect
}