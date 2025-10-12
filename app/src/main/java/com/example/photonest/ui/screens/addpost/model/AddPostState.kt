package com.example.photonest.ui.screens.addpost.model

import android.net.Uri
import com.example.photonest.data.model.User

data class AddPostState(
    val selectedImageUri: Uri? = null,
    val caption: String = "",
    val location: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isPostCreated: Boolean = false,
    val error: String? = null,
    val showErrorDialog: Boolean = false,
    val currentUser: User? = null
)