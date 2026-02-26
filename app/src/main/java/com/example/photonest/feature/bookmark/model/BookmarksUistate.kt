package com.example.photonest.feature.bookmark.model

import com.example.photonest.data.model.Post

data class BookmarksUiState(
    val isLoading: Boolean = false,
    val bookmarkedPosts: List<Post> = emptyList(),
    val isGridView: Boolean = true,
    val error: String? = null,
    val showErrorDialog: Boolean = false
)
