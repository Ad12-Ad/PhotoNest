package com.example.photonest.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.ui.screens.bookmarks.model.BookmarksEvent
import com.example.photonest.ui.screens.bookmarks.model.BookmarksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val postRepository: IPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarkedPosts()
    }

    fun onEvent(event: BookmarksEvent) {
        when (event) {
            is BookmarksEvent.RefreshBookmarks -> {
                loadBookmarkedPosts()
            }
            is BookmarksEvent.ToggleViewType -> {
                _uiState.update { currentState ->
                    currentState.copy(isGridView = !currentState.isGridView)
                }
            }
            is BookmarksEvent.ToggleBookmark -> {
                toggleBookmark(event.postId)
            }
            is BookmarksEvent.ToggleLike -> {
                toggleLike(event.postId)
            }
            is BookmarksEvent.DismissErrorDialog -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        showErrorDialog = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadBookmarkedPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = postRepository.getBookmarkedPosts()) {
                is Resource.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            bookmarkedPosts = result.data ?: emptyList(),
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load bookmarks",
                            showErrorDialog = true
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.bookmarkedPosts.find { it.id == postId }
            if (currentPost?.isBookmarked == true) {
                // Remove from bookmarks
                when (val result = postRepository.unbookmarkPost(postId)) {
                    is Resource.Success -> {
                        // Remove post from the list since it's no longer bookmarked
                        _uiState.update { currentState ->
                            currentState.copy(
                                bookmarkedPosts = currentState.bookmarkedPosts.filter { it.id != postId }
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                error = result.message ?: "Failed to remove bookmark",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.bookmarkedPosts.find { it.id == postId }
            if (currentPost != null) {
                val result = if (currentPost.isLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                when (result) {
                    is Resource.Success -> {
                        // Update the post in the list
                        _uiState.update { currentState ->
                            currentState.copy(
                                bookmarkedPosts = currentState.bookmarkedPosts.map { post ->
                                    if (post.id == postId) {
                                        post.copy(
                                            isLiked = !post.isLiked,
                                            likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                                        )
                                    } else {
                                        post
                                    }
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                error = result.message ?: "Failed to toggle like",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}
