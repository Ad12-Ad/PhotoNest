package com.example.photonest.feature.explore

import com.example.photonest.domain.model.Category
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.SearchResult
import com.example.photonest.domain.model.User

data class ExploreState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // Search
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: SearchResult = SearchResult(),

    // Explore content
    val trendingPosts: List<Post> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val trendingCategories: List<Category> = emptyList()
)

sealed interface ExploreEvent {

    // Search
    data class UpdateQuery(val query: String) : ExploreEvent
    data object SubmitSearch : ExploreEvent
    data object ClearSearch : ExploreEvent
    data class SearchByCategory(val category: String) : ExploreEvent

    // Explore
    data object LoadExplore : ExploreEvent
    data object Refresh : ExploreEvent

    // User actions
    data class FollowUser(val userId: String) : ExploreEvent
    data class OpenProfile(val userId: String) : ExploreEvent
    data class OpenPost(val postId: String) : ExploreEvent
}

sealed interface ExploreEffect {
    data class ShowError(val message: String) : ExploreEffect
    data class NavigateToProfile(val userId: String) : ExploreEffect
    data class NavigateToPost(val postId: String) : ExploreEffect
}