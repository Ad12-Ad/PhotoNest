package com.example.photonest.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
        observeUser()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            postRepository.getPosts()
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    posts = result.data ?: emptyList(),
                                    error = null
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(currentUser = result.data)
                            }
                        }
                        is Resource.Error -> {
                            // Handle user error if needed
                        }
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                    }
                }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.posts.find { it.id == postId }
            if (currentPost != null) {
                val result = if (currentPost.isLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                when (result) {
                    is Resource.Success -> {
                        // Update local state optimistically
                        val updatedPosts = _uiState.value.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    isLiked = !post.isLiked,
                                    likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                                )
                            } else {
                                post
                            }
                        }
                        _uiState.update { it.copy(posts = updatedPosts) }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val currentPost = _uiState.value.posts.find { it.id == postId }
            if (currentPost != null) {
                val result = if (currentPost.isBookmarked) {
                    postRepository.unbookmarkPost(postId)
                } else {
                    postRepository.bookmarkPost(postId)
                }

                when (result) {
                    is Resource.Success -> {
                        // Update local state optimistically
                        val updatedPosts = _uiState.value.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(isBookmarked = !post.isBookmarked)
                            } else {
                                post
                            }
                        }
                        _uiState.update { it.copy(posts = updatedPosts) }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun onPostClick(postId: String) {
        // Handle post click - navigate to post detail
    }

    fun onUserClick(userId: String) {
        // Handle user click - navigate to user profile
    }

    fun refreshPosts() {
        loadPosts()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val currentUser: com.example.photonest.data.model.User? = null,
    val error: String? = null
)
