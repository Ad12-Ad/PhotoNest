package com.example.photonest.feature.feed.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.ToggleBookmarkUseCase
import com.example.photonest.domain.usecase.ToggleFollowUseCase
import com.example.photonest.domain.usecase.ToggleLikeUseCase
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
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val userRepository: IUserRepository,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase
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
            is HomeUiEvent.PostClicked -> emitEffect(HomeUiEffect.NavigateToPost(event.postId))
            is HomeUiEvent.UserClicked -> emitEffect(HomeUiEffect.NavigateToUser(event.userId))
            is HomeUiEvent.ToggleLike -> toggleLike(event.postId)
            is HomeUiEvent.ToggleBookmark -> toggleBookmark(event.postId)
            is HomeUiEvent.ToggleFollow -> toggleFollow(event.userId, event.postId)
            is HomeUiEvent.FollowSuggestedUser -> followSuggestedUser(event.userId)
            is HomeUiEvent.OpenLikes -> openLikes(event.postId)
            HomeUiEvent.CloseLikes -> _state.update { it.copy(isLikesSheetVisible = false) }
            is HomeUiEvent.SharePost -> sharePost(event.postId)
            HomeUiEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadPosts() = viewModelScope.launch {
        postRepository.getPosts().collect { result ->
            when (result) {
                is NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
                is NetworkResult.Success -> {
                    val loadedPosts = result.data.orEmpty()
                    _state.update {
                        it.copy(isLoading = false, posts = loadedPosts, isRefreshing = false)
                    }

                    if (loadedPosts.isEmpty()) {
                        loadSuggestions()
                    }
                }
                is NetworkResult.Error -> emitError(result.message ?: "Failed to load feed")
            }
        }
    }

    private fun loadSuggestions() = viewModelScope.launch {
        val trending = postRepository.getTrendingPosts().data.orEmpty()
        val users = userRepository.getPopularUsers().data.orEmpty()
        _state.update { it.copy(trendingPosts = trending, suggestedUsers = users) }
    }

    private fun followSuggestedUser(userId: String) = viewModelScope.launch {
        val wasFollowing = false // Assuming suggested users are unfollowed

        // Optimistic UI update
        _state.update { state ->
            state.copy(
                suggestedUsers = state.suggestedUsers.map { u ->
                    if (u.id == userId) u.copy(followersCount = u.followersCount + 1) else u
                }
            )
        }

        val result = toggleFollowUseCase(targetUserId = userId, isCurrentlyFollowing = wasFollowing)

        if (result is NetworkResult.Error) {
            // Rollback
            _state.update { state ->
                state.copy(
                    suggestedUsers = state.suggestedUsers.map { u ->
                        if (u.id == userId) u.copy(followersCount = u.followersCount - 1) else u
                    }
                )
            }
            emitError(result.message ?: "Failed to follow user")
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
                posts = it.posts.map { p ->
                    if (p.id == postId) p.copy(
                        isLiked = !p.isLiked,
                        likeCount = if (p.isLiked) p.likeCount - 1 else p.likeCount + 1
                    ) else p
                }
            )
        }

        // Delegated to UseCase - Notifications are handled automatically!
        val result = toggleLikeUseCase(post)

        if (result is NetworkResult.Error) rollback(post)
    }

    private fun toggleBookmark(postId: String) = viewModelScope.launch {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch

        optimisticUpdate {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.id == postId) p.copy(isBookmarked = !p.isBookmarked) else p
                }
            )
        }

        // Delegated to UseCase
        val result = toggleBookmarkUseCase(postId, post.isBookmarked)

        if (result is NetworkResult.Error) rollback(post)
    }

    private fun toggleFollow(userId: String, postId: String) = viewModelScope.launch {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch
        val wasFollowing = post.isUserFollowed

        optimisticUpdate {
            it.copy(
                posts = it.posts.map { p ->
                    if (p.userId == userId) p.copy(isUserFollowed = !wasFollowing) else p
                }
            )
        }

        val result = toggleFollowUseCase(targetUserId = userId, isCurrentlyFollowing = wasFollowing)

        if (result is NetworkResult.Error) {
            rollback(post)
            emitError(result.message ?: "Failed to update follow status")
        }
    }

    private fun openLikes(postId: String) = viewModelScope.launch {
        _state.update { it.copy(isLikesSheetVisible = true, isLikesLoading = true, likesPostId = postId) }

        when (val result = postRepository.getUsersWhoLikedPost(postId)) {
            is NetworkResult.Success -> _state.update {
                it.copy(likedUsers = result.data.orEmpty(), isLikesLoading = false)
            }
            is NetworkResult.Error -> _state.update { it.copy(isLikesLoading = false) }
            else -> Unit
        }
    }

    private fun sharePost(postId: String) {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return
        emitEffect(HomeUiEffect.Share("${post.caption}\n\nby @${post.userName}"))
    }

    private fun rollback(post: Post) {
        _state.update { it.copy(posts = it.posts.map { p -> if (p.id == post.id) post else p }) }
    }

    private fun optimisticUpdate(block: (HomeUiState) -> HomeUiState) {
        _state.update(block)
    }

    private fun emitError(message: String) {
        _state.update { it.copy(error = message, isLoading = false) }
        emitEffect(HomeUiEffect.ShowError(message))
    }

    private fun emitEffect(effect: HomeUiEffect) = viewModelScope.launch { _effect.emit(effect) }
}