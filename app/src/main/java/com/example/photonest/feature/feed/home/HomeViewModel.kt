package com.example.photonest.feature.feed.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.feature.feed.home.model.HomeUiEffect
import com.example.photonest.feature.feed.home.model.HomeUiEvent
import com.example.photonest.feature.feed.home.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val postDao: PostDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect = _effect.asSharedFlow()

    init {
        onEvent(HomeUiEvent.Load)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Load -> loadPosts()
            HomeUiEvent.Refresh -> refresh()

            is HomeUiEvent.PostClicked ->
                emitEffect(HomeUiEffect.NavigateToPost(event.postId))

            is HomeUiEvent.UserClicked ->
                emitEffect(HomeUiEffect.NavigateToUser(event.userId))

            is HomeUiEvent.ToggleLike -> toggleLike(event.postId)
            is HomeUiEvent.ToggleBookmark -> toggleBookmark(event.postId)
            is HomeUiEvent.ToggleFollow ->
                toggleFollow(event.userId, event.postId)

            is HomeUiEvent.OpenLikes -> openLikes(event.postId)
            HomeUiEvent.CloseLikes ->
                _state.update { it.copy(isLikesSheetVisible = false) }

            is HomeUiEvent.SharePost -> sharePost(event.postId)
            HomeUiEvent.DismissError ->
                _state.update { it.copy(error = null) }
        }
    }

    private fun loadPosts() = viewModelScope.launch {
        postRepository.getPosts().collect { result ->
            when (result) {
                is NetworkResult.Loading ->
                    _state.update { it.copy(isLoading = true) }

                is NetworkResult.Success ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            posts = result.data.orEmpty(),
                            isRefreshing = false
                        )
                    }

                is NetworkResult.Error ->
                    emitError(result.message ?: "Failed to load feed")
            }
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadPosts()
    }

    private fun toggleLike(postId: String) = viewModelScope.launch {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch

        optimisticUpdate {
            it.copy(
                posts = it.posts.map {
                    if (it.id == postId)
                        it.copy(
                            isLiked = !it.isLiked,
                            likeCount =
                                if (it.isLiked) it.likeCount - 1
                                else it.likeCount + 1
                        )
                    else it
                }
            )
        }

        val result =
            if (post.isLiked) postRepository.unlikePost(postId)
            else postRepository.likePost(postId)

        if (result is NetworkResult.Error) rollback(post)
    }

    private fun toggleBookmark(postId: String) = viewModelScope.launch {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch

        optimisticUpdate {
            it.copy(
                posts = it.posts.map {
                    if (it.id == postId)
                        it.copy(isBookmarked = !it.isBookmarked)
                    else it
                }
            )
        }

        val result =
            if (post.isBookmarked)
                postRepository.unbookmarkPost(postId)
            else
                postRepository.bookmarkPost(postId)

        if (result is NetworkResult.Error) rollback(post)
    }

    private fun toggleFollow(userId: String, postId: String) = viewModelScope.launch {
        val currentUserId = authRepository.getCurrentUserId() ?: return@launch
        if (currentUserId == userId) return@launch

        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch
        val wasFollowing = post.isUserFollowed

        optimisticUpdate {
            it.copy(
                posts = it.posts.map {
                    if (it.userId == userId)
                        it.copy(isUserFollowed = !wasFollowing)
                    else it
                }
            )
        }

        val result =
            if (wasFollowing)
                userRepository.unfollowUser(userId)
            else
                userRepository.followUser(userId)

        if (result is NetworkResult.Error) rollback(post)
        if (wasFollowing && result is NetworkResult.Success) {
            postDao.deletePostsByUser(userId)
        }
    }

    private fun openLikes(postId: String) = viewModelScope.launch {
        _state.update {
            it.copy(
                isLikesSheetVisible = true,
                isLikesLoading = true,
                likesPostId = postId
            )
        }

        when (val result = postRepository.getUsersWhoLikedPost(postId)) {
            is NetworkResult.Success ->
                _state.update {
                    it.copy(
                        likedUsers = result.data.orEmpty(),
                        isLikesLoading = false
                    )
                }

            is NetworkResult.Error ->
                _state.update { it.copy(isLikesLoading = false) }

            else -> Unit
        }
    }

    private fun sharePost(postId: String) {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return
        emitEffect(
            HomeUiEffect.Share(
                "${post.caption}\n\nby @${post.userName}"
            )
        )
    }

    private fun rollback(post: Post) {
        _state.update {
            it.copy(
                posts = it.posts.map {
                    if (it.id == post.id) post else it
                }
            )
        }
    }

    private fun optimisticUpdate(block: (HomeUiState) -> HomeUiState) {
        _state.update(block)
    }

    private fun emitError(message: String) {
        _state.update { it.copy(error = message, isLoading = false) }
        emitEffect(HomeUiEffect.ShowError(message))
    }

    private fun emitEffect(effect: HomeUiEffect) =
        viewModelScope.launch { _effect.emit(effect) }
}