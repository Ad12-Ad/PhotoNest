package com.example.photonest.ui.screens.addpost.model

import android.net.Uri

sealed class AddPostEvent {
    data class ImageSelected(val uri: Uri?) : AddPostEvent()
    data class CategoryToggled(val category: String) : AddPostEvent()
    data class SearchQueryChanged(val query: String) : AddPostEvent()
    object ClearCategories : AddPostEvent()
    object PostClicked : AddPostEvent()
    object DismissErrorDialog : AddPostEvent()
}