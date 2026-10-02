package com.example.photonest.feature.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.usecase.ToggleBookmarkUseCase
import com.example.photonest.domain.usecase.ToggleLikeUseCase
import com.example.photonest.feature.bookmark.model.BookmarksEvent
import com.example.photonest.feature.bookmark.model.BookmarksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init { loadBookmarkedPosts() }

    fun onEvent(event: BookmarksEvent) {
        when (event) {
            is BookmarksEvent.RefreshBookmarks -> loadBookmarkedPosts()
            is BookmarksEvent.ToggleViewType -> _uiState.update { it.copy(isGridView = !it.isGridView) }
            is BookmarksEvent.ToggleBookmark -> toggleBookmark(event.postId)
            is BookmarksEvent.ToggleLike -> toggleLike(event.postId)
            is BookmarksEvent.DismissErrorDialog -> _uiState.update { it.copy(showErrorDialog = false, error = null) }
        }
    }

    private fun loadBookmarkedPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = withContext(Dispatchers.IO) { postRepository.getBookmarkedPosts() }) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, bookmarkedPosts = result.data ?: emptyList(), error = null) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load bookmarks", showErrorDialog = true) }
                }
                is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.bookmarkedPosts.find { it.id == postId } ?: return@launch

            // Optimistic UI update: instantly remove it from the bookmarks view
            val cachedList = _uiState.value.bookmarkedPosts
            _uiState.update { it.copy(bookmarkedPosts = cachedList.filter { p -> p.id != postId }) }

            val result = toggleBookmarkUseCase(postId, currentPost.isBookmarked)

            if (result is NetworkResult.Error) {
                // Rollback if the network call failed
                _uiState.update {
                    it.copy(
                        bookmarkedPosts = cachedList,
                        error = result.message ?: "Failed to remove bookmark",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    private fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.bookmarkedPosts.find { it.id == postId } ?: return@launch

            // Optimistic UI update
            _uiState.update { currentState ->
                currentState.copy(
                    bookmarkedPosts = currentState.bookmarkedPosts.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                isLiked = !post.isLiked,
                                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                            )
                        } else post
                    }
                )
            }

            // Delegated to UseCase (Handles the notification rules automatically)
            val result = toggleLikeUseCase(currentPost)

            if (result is NetworkResult.Error) {
                // Rollback
                _uiState.update { currentState ->
                    currentState.copy(
                        bookmarkedPosts = currentState.bookmarkedPosts.map { post ->
                            if (post.id == postId) currentPost else post
                        },
                        error = result.message ?: "Failed to toggle like",
                        showErrorDialog = true
                    )
                }
            }
        }
    }
}