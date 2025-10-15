package com.example.photonest.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.*
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
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
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadExploreContent()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                isSearchActive = query.isNotEmpty()
            )
        }

        if (query.isNotEmpty()) {
            performSearchWithDelay(query)
        } else {
            clearSearch()
        }
    }

    private fun performSearchWithDelay(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            performSearchInternal(query)
        }
    }

    fun performSearch(query: String? = null) {
        val searchQuery = query ?: _uiState.value.searchQuery
        if (searchQuery.isNotEmpty()) {
            performSearchInternal(searchQuery)
        }
    }

    private fun performSearchInternal(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Search users
                val usersResult = userRepository.searchUsers(query)
                val users = when (usersResult) {
                    is Resource.Success -> usersResult.data ?: emptyList()
                    else -> emptyList()
                }

                // Search posts
                val postsResult = postRepository.searchPosts(query)
                val posts = when (postsResult) {
                    is Resource.Success -> postsResult.data ?: emptyList()
                    else -> emptyList()
                }

                // Create dummy categories for search (in real app, would search categories)
                val categories = getDummyCategories().filter {
                    it.name.lowercase().contains(query.lowercase())
                }

                val searchResults = SearchResult(
                    users = users,
                    posts = posts,
                    categories = categories,
                    totalResults = users.size + posts.size + categories.size,
                    query = query
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = searchResults,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Search failed",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    fun searchByCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchQuery = categoryName,
                    isSearchActive = true,
                    isLoading = true,
                    error = null
                )
            }

            val result = postRepository.getPostsByCategory(categoryName)

            when (result) {
                is Resource.Success -> {
                    val searchResults = SearchResult(
                        posts = result.data ?: emptyList(),
                        users = emptyList(),
                        categories = emptyList(),
                        totalResults = result.data?.size ?: 0,
                        query = categoryName
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = searchResults,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load category posts",
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

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                isSearchActive = false,
                searchResults = SearchResult(),
                error = null
            )
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch

            if (currentUserId == userId) {
                _uiState.update {
                    it.copy(
                        error = "Cannot follow yourself",
                        showErrorDialog = true
                    )
                }
                return@launch
            }

            // Check current follow status
            val isFollowingResult = userRepository.isFollowing(userId)
            val isCurrentlyFollowing = when (isFollowingResult) {
                is Resource.Success -> isFollowingResult.data == true
                else -> false
            }

            val result = if (isCurrentlyFollowing) {
                userRepository.unfollowUser(userId)
            } else {
                userRepository.followUser(userId)
            }

            when (result) {
                is Resource.Success -> {
                    // Reload fresh data from Firestore
                    loadExploreContent()
                }
                is Resource.Error -> {
                    if (!result.message.orEmpty().contains("Already following", ignoreCase = true) &&
                        !result.message.orEmpty().contains("Cannot follow yourself", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to update follow status",
                                showErrorDialog = true
                            )
                        }
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun refreshContent() {
        loadExploreContent()
    }

    fun dismissError() {
        _uiState.update {
            it.copy(
                error = null,
                showErrorDialog = false
            )
        }
    }

    private fun loadExploreContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load trending posts
                val trendingPostsResult = postRepository.getTrendingPosts()
                val trendingPosts = when (trendingPostsResult) {
                    is Resource.Success -> trendingPostsResult.data ?: emptyList()
                    else -> getDummyTrendingPosts() // Fallback to dummy data
                }

                // Load suggested users
                val suggestedUsersResult = userRepository.getPopularUsers()
                val suggestedUsers = when (suggestedUsersResult) {
                    is Resource.Success -> suggestedUsersResult.data ?: emptyList()
                    else -> getDummySuggestedUsers() // Fallback to dummy data
                }

                // Load trending categories (dummy data for now)
                val trendingCategories = getDummyCategories()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trendingPosts = trendingPosts,
                        suggestedUsers = suggestedUsers,
                        trendingCategories = trendingCategories,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load content",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    private fun getDummyTrendingPosts(): List<Post> {
        return listOf(
            Post(
                id = "trending1",
                userId = "user1",
                userName = "Alex Johnson",
                userImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face",
                imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=400&fit=crop",
                caption = "Mountain sunrise capturing the golden hour magic ✨ #nature #photography",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                category = listOf("Nature", "Photography", "Landscape"),
                likeCount = 15420,
                commentCount = 234,
                shareCount = 89,
                location = "Swiss Alps",
                isLiked = false,
                isBookmarked = false,
                tags = listOf("sunrise", "mountains", "golden_hour"),
                aspectRatio = 4f/3f
            ),
            Post(
                id = "trending2",
                userId = "user2",
                userName = "Maya Chen",
                userImage = "https://images.unsplash.com/photo-1494790108755-2616b612b429?w=150&h=150&fit=crop&crop=face",
                imageUrl = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=600&fit=crop",
                caption = "Street art in Tokyo's vibrant neighborhoods 🎨 #streetart #tokyo #urban",
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                category = listOf("Street", "Art", "Urban"),
                likeCount = 8930,
                commentCount = 156,
                shareCount = 45,
                location = "Shibuya, Tokyo",
                isLiked = true,
                isBookmarked = false,
                tags = listOf("streetart", "tokyo", "shibuya"),
                aspectRatio = 2f/3f
            )
        )
    }

    private fun getDummySuggestedUsers(): List<User> {
        return listOf(
            User(
                id = "suggested1",
                email = "david@example.com",
                name = "David Rodriguez",
                username = "davidphotos",
                profilePicture = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                bio = "Travel photographer capturing moments around the world 📸",
                location = "Barcelona, Spain",
                postsCount = 127,
                followersCount = 3420,
                followingCount = 892,
                isVerified = true
            ),
            User(
                id = "suggested2",
                email = "sarah@example.com",
                name = "Sarah Kim",
                username = "sarahartist",
                profilePicture = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop&crop=face",
                bio = "Digital artist & designer creating vibrant illustrations ✨",
                location = "Seoul, South Korea",
                postsCount = 89,
                followersCount = 2150,
                followingCount = 456,
                isVerified = false
            )
        )
    }

    private fun getDummyCategories(): List<Category> {
        return listOf(
            Category(
                id = "cat1",
                name = "Nature",
                imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=150&h=150&fit=crop",
                color = "#4CAF50",
                postsCount = 15420,
                isPopular = true
            ),
            Category(
                id = "cat2",
                name = "Photography",
                imageUrl = "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=150&h=150&fit=crop",
                color = "#2196F3",
                postsCount = 12340,
                isPopular = true
            ),
            Category(
                id = "cat3",
                name = "Art",
                imageUrl = "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=150&h=150&fit=crop",
                color = "#E91E63",
                postsCount = 9876,
                isPopular = true
            ),
            Category(
                id = "cat4",
                name = "Travel",
                imageUrl = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=150&h=150&fit=crop",
                color = "#FF9800",
                postsCount = 8765,
                isPopular = true
            )
        )
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
