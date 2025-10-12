package com.example.photonest.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val showErrorDialog: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val postRepository: IPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            postRepository.getPosts().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                posts = result.data ?: emptyList(),
                                error = null,
                                isRefreshing = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load posts",
                                showErrorDialog = true,
                                isRefreshing = false
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { currentState ->
                            currentState.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    fun refreshPosts() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPosts()
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val post = _uiState.value.posts.find { it.id == postId }
            if (post != null) {
                val result = if (post.isLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = currentState.posts.map {
                                    if (it.id == postId) {
                                        it.copy(
                                            isLiked = !it.isLiked,
                                            likeCount = if (it.isLiked) it.likeCount - 1 else it.likeCount + 1
                                        )
                                    } else it
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        showError(result.message ?: "Failed to toggle like")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val post = _uiState.value.posts.find { it.id == postId }
            if (post != null) {
                val result = if (post.isBookmarked) {
                    postRepository.unbookmarkPost(postId)
                } else {
                    postRepository.bookmarkPost(postId)
                }

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = currentState.posts.map {
                                    if (it.id == postId) {
                                        it.copy(isBookmarked = !it.isBookmarked)
                                    } else it
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        showError(result.message ?: "Failed to toggle bookmark")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun sharePost(post: Post, context: Context) {
        val shareText = buildString {
            appendLine("Check out this post on PhotoNest!")
            appendLine()
            appendLine("${post.caption}")
            if (post.location.isNotEmpty()) {
                appendLine("📍 ${post.location}")
            }
            appendLine()
            appendLine("by @${post.userName}")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
    }

    fun dismissErrorDialog() {
        _uiState.update {
            it.copy(showErrorDialog = false, error = null)
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(error = message, showErrorDialog = true)
        }
    }
}
