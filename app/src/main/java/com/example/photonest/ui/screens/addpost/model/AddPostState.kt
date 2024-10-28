package com.example.photonest.ui.screens.addpost.model

import android.net.Uri

data class AddPostState(
    val selectedImageUri: Uri? = null,
    val selectedCategories: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showErrorDialog: Boolean = false
)