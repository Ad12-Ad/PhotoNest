package com.example.photonest.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.User
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFollowStates = _uiState.value.posts
                .associate { it.userId to it.isUserFollowed }

            postRepository.getPosts().collect { result ->
                withContext(Dispatchers.Main){
                    when (result) {
                        is Resource.Success -> {
                            val posts = result.data?.map { post ->
                                val savedFollowState = currentFollowStates[post.userId]
                                if (savedFollowState != null) {
                                    // Use the preserved optimistic state
                                    post.copy(isUserFollowed = savedFollowState)
                                } else {
                                    // Use fresh state from Firestore
                                    post
                                }
                            } ?: emptyList()

                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    posts = posts,
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
    }

    fun refreshPosts() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPosts()
    }

    fun toggleFollow(userId: String, postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            if (currentUserId == userId) return@launch

            val post = _uiState.value.posts.find { it.id == postId } ?: return@launch
            val isCurrentlyFollowing = post.isUserFollowed

            withContext(Dispatchers.Main){
                _uiState.update { currentState ->
                    currentState.copy(
                        posts = currentState.posts.map { p ->
                            if (p.userId == userId) {
                                p.copy(isUserFollowed = !isCurrentlyFollowing)
                            } else p
                        }
                    )
                }
            }

            val result = if (isCurrentlyFollowing) {
                userRepository.unfollowUser(userId)
            } else {
                userRepository.followUser(userId)
            }

            withContext(Dispatchers.Main){
                when (result) {
                    is Resource.Success -> {
                        if (isCurrentlyFollowing) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    posts = currentState.posts.filter { it.userId != userId }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = currentState.posts.map { p ->
                                    if (p.userId == userId) {
                                        p.copy(isUserFollowed = isCurrentlyFollowing)
                                    } else p
                                }
                            )
                        }

                        if (!result.message.orEmpty().contains("Already following", ignoreCase = true) &&
                            !result.message.orEmpty().contains("Cannot follow yourself", ignoreCase = true)) {
                            showError(result.message ?: "Failed to update follow status")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = _uiState.value.posts.find { it.id == postId } ?: return@launch

            withContext(Dispatchers.Main){
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

            val result = if (post.isLiked) {
                postRepository.unlikePost(postId)
            } else {
                postRepository.likePost(postId)
            }

            withContext(Dispatchers.Main){
                when (result) {
                    is Resource.Error -> {
                        // Revert on error
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = currentState.posts.map {
                                    if (it.id == postId) {
                                        it.copy(
                                            isLiked = post.isLiked,
                                            likeCount = post.likeCount
                                        )
                                    } else it
                                }
                            )
                        }
                        showError(result.message ?: "Failed to toggle like")
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = _uiState.value.posts.find { it.id == postId } ?: return@launch

            withContext(Dispatchers.Main){
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

            val result = if (post.isBookmarked) {
                postRepository.unbookmarkPost(postId)
            } else {
                postRepository.bookmarkPost(postId)
            }

            withContext(Dispatchers.Main){
                when (result) {
                    is Resource.Error -> {
                        // Revert on error
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = currentState.posts.map {
                                    if (it.id == postId) {
                                        it.copy(isBookmarked = post.isBookmarked)
                                    } else it
                                }
                            )
                        }
                        showError(result.message ?: "Failed to toggle bookmark")
                    }
                    else -> {}
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

    fun loadUsersWhoLiked(postId: String, onResult: (List<User>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = postRepository.getUsersWhoLikedPost(postId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> {
                            onResult(result.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            onResult(emptyList())
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.followUser(userId)
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.unfollowUser(userId)
        }
    }

    fun toggleFollowFromBottomSheet(userId: String, isCurrentlyFollowing: Boolean, postId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            if (currentUserId == userId) return@launch

            val result = if (isCurrentlyFollowing) {
                userRepository.unfollowUser(userId)
            } else {
                userRepository.followUser(userId)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        postId?.let { id ->
                            loadUsersWhoLiked(id) { /* callback updates automatically */ }
                        }
                    }
                    is Resource.Error -> {
                        if (!result.message.orEmpty().contains("Already following", ignoreCase = true) &&
                            !result.message.orEmpty().contains("Cannot follow yourself", ignoreCase = true)) {
                            showError(result.message ?: "Failed to update follow status")
                        }
                    }
                    else -> {}
                }
            }
        }
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
