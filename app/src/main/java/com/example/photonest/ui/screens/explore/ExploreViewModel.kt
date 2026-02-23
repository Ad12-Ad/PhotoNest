package com.example.photonest.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.*
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.FollowUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository,
    private val followUserUseCase: FollowUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init { loadExploreContent() }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotEmpty()) }
        if (query.isNotEmpty()) performSearchWithDelay(query)
        else clearSearch()
    }

    private fun performSearchWithDelay(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            performSearchInternal(query)
        }
    }

    fun performSearch(query: String? = null) {
        val q = query ?: _uiState.value.searchQuery
        if (q.isNotEmpty()) viewModelScope.launch { performSearchInternal(q) }
    }

    private suspend fun performSearchInternal(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val users = withContext(Dispatchers.IO) {
                when (val r = userRepository.searchUsers(query)) {
                    is NetworkResult.Success -> r.data ?: emptyList()
                    else -> emptyList()
                }
            }
            val posts = withContext(Dispatchers.IO) {
                when (val r = postRepository.searchPosts(query)) {
                    is NetworkResult.Success -> r.data ?: emptyList()
                    else -> emptyList()
                }
            }
            val searchResults = SearchResult(
                users = users, posts = posts, categories = emptyList(),
                totalResults = users.size + posts.size, query = query
            )
            _uiState.update { it.copy(isLoading = false, searchResults = searchResults, error = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "Search failed") }
        }
    }

    fun followUser(userId: String) {
        // Optimistic UI update first
        _uiState.update { state ->
            state.copy(suggestedUsers = state.suggestedUsers.map { user ->
                if (user.id == userId) {
                    user.copy(followersCount = user.followersCount + 1)
                } else user
            })
        }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { followUserUseCase(userId) }
            when (result) {
                is NetworkResult.Error -> {
                    // Rollback optimistic update
                    _uiState.update { state ->
                        state.copy(
                            suggestedUsers = state.suggestedUsers.map { user ->
                                if (user.id == userId) user.copy(followersCount = user.followersCount - 1)
                                else user
                            },
                            error = result.message ?: "Failed to update follow status"
                        )
                    }
                }
                else -> { /* optimistic update stays */ }
            }
        }
    }

    fun searchByCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = categoryName, isSearchActive = true, isLoading = true) }
            val result = withContext(Dispatchers.IO) { postRepository.getPostsByCategory(categoryName) }
            when (result) {
                is NetworkResult.Success -> {
                    val sr = SearchResult(posts = result.data ?: emptyList(), totalResults = result.data?.size ?: 0, query = categoryName)
                    _uiState.update { it.copy(isLoading = false, searchResults = sr) }
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = "", isSearchActive = false, searchResults = SearchResult()) }
    }

    fun refreshContent() { loadExploreContent() }

    fun dismissError() { _uiState.update { it.copy(error = null, showErrorDialog = false) } }

    private fun loadExploreContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val trendingPosts = withContext(Dispatchers.IO) {
                    when (val r = postRepository.getTrendingPosts()) {
                        is NetworkResult.Success -> r.data ?: emptyList()
                        else -> emptyList<Post>()
                    }
                }
                val suggestedUsers = withContext(Dispatchers.IO) {
                    when (val r = userRepository.getPopularUsers()) {
                        is NetworkResult.Success -> r.data ?: emptyList()
                        else -> emptyList<User>()
                    }
                }
                _uiState.update {
                    it.copy(isLoading = false, trendingPosts = trendingPosts, suggestedUsers = suggestedUsers)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load content") }
            }
        }
    }
}

data class ExploreUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: SearchResult = SearchResult(),
    val trendingPosts: List<Post> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val trendingCategories: List<Category> = emptyList(),
    val error: String? = null,
    val showErrorDialog: Boolean = false
)