package com.example.photonest.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.*
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.FollowUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val followUserUseCase: FollowUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ExploreEffect>()
    val effect = _effect.asSharedFlow()

    private var searchJob: Job? = null

    init {
        onEvent(ExploreEvent.LoadExplore)
    }

    fun onEvent(event: ExploreEvent) {
        when (event) {

            is ExploreEvent.UpdateQuery -> updateQuery(event.query)
            ExploreEvent.SubmitSearch -> performSearch()
            ExploreEvent.ClearSearch -> clearSearch()
            is ExploreEvent.SearchByCategory -> searchByCategory(event.category)

            ExploreEvent.LoadExplore,
            ExploreEvent.Refresh -> loadExplore()

            is ExploreEvent.FollowUser -> followUser(event.userId)

            is ExploreEvent.OpenProfile ->
                emitEffect(ExploreEffect.NavigateToProfile(event.userId))

            is ExploreEvent.OpenPost ->
                emitEffect(ExploreEffect.NavigateToPost(event.postId))
        }
    }

    /* ---------------- Search ---------------- */

    private fun updateQuery(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                isSearchActive = query.isNotBlank()
            )
        }

        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(400)
                performSearch()
            }
        }
    }

    private fun performSearch() = viewModelScope.launch {
        val query = state.value.searchQuery
        if (query.isBlank()) return@launch

        _state.update { it.copy(isLoading = true) }

        try {
            val users = when (val r = userRepository.searchUsers(query)) {
                is NetworkResult.Success -> r.data ?: emptyList()
                else -> emptyList()
            }

            val posts = when (val r = postRepository.searchPosts(query)) {
                is NetworkResult.Success -> r.data ?: emptyList()
                else -> emptyList()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    searchResults = SearchResult(
                        users = users,
                        posts = posts,
                        totalResults = users.size + posts.size,
                        query = query
                    )
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
            emitEffect(
                ExploreEffect.ShowError(
                    e.message ?: "Search failed"
                )
            )
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        _state.update {
            it.copy(
                searchQuery = "",
                isSearchActive = false,
                searchResults = SearchResult()
            )
        }
    }

    private fun searchByCategory(category: String) = viewModelScope.launch {
        _state.update {
            it.copy(
                searchQuery = category,
                isSearchActive = true,
                isLoading = true
            )
        }

        when (val result = postRepository.getPostsByCategory(category)) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        searchResults = SearchResult(
                            posts = result.data ?: emptyList(),
                            totalResults = result.data?.size ?: 0,
                            query = category
                        )
                    )
                }
            }

            is NetworkResult.Error -> {
                _state.update { it.copy(isLoading = false) }
                emitEffect(
                    ExploreEffect.ShowError(
                        result.message ?: "Failed to load category"
                    )
                )
            }

            else -> Unit
        }
    }

    /* ---------------- Explore ---------------- */

    private fun loadExplore() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        try {
            val trendingPosts = when (val r = postRepository.getTrendingPosts()) {
                is NetworkResult.Success -> r.data ?: emptyList()
                else -> emptyList()
            }

            val users = when (val r = userRepository.getPopularUsers()) {
                is NetworkResult.Success -> r.data ?: emptyList()
                else -> emptyList()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    trendingPosts = trendingPosts,
                    suggestedUsers = users
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
            emitEffect(
                ExploreEffect.ShowError(
                    e.message ?: "Failed to load explore content"
                )
            )
        }
    }

    /* ---------------- Follow ---------------- */

    private fun followUser(userId: String) = viewModelScope.launch {
        // optimistic
        _state.update {
            it.copy(
                suggestedUsers = it.suggestedUsers.map { u ->
                    if (u.id == userId)
                        u.copy(followersCount = u.followersCount + 1)
                    else u
                }
            )
        }

        val result = followUserUseCase(userId)

        if (result is NetworkResult.Error) {
            // rollback
            _state.update {
                it.copy(
                    suggestedUsers = it.suggestedUsers.map { u ->
                        if (u.id == userId)
                            u.copy(followersCount = u.followersCount - 1)
                        else u
                    }
                )
            }

            emitEffect(
                ExploreEffect.ShowError(
                    result.message ?: "Failed to follow user"
                )
            )
        }
    }

    private fun emitEffect(effect: ExploreEffect) =
        viewModelScope.launch { _effect.emit(effect) }
}