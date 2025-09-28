package com.example.photonest.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.photonest.R
import com.example.photonest.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        val dummyPosts = listOf(
            Post(
                id = "1",
                userId = "user1",
                userName = "John Doe",
                userImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face",
                imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=400&fit=crop",
                caption = "Beautiful nature photography! Captured this amazing sunset during my hiking trip. The colors were absolutely breathtaking! 🌅 #nature #photography #sunset",
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                category = listOf("Nature", "Photography", "Sunset"),
                likeCount = 3300,
                commentCount = 45,
                shareCount = 12,
                location = "Yosemite National Park, CA",
                isLiked = false,
                isBookmarked = false,
                tags = listOf("hiking", "mountains", "california"),
                aspectRatio = 4f/3f
            ),
            Post(
                id = "2",
                userId = "user2",
                userName = "Jane Smith",
                userImage = "https://images.unsplash.com/photo-1494790108755-2616b612b429?w=150&h=150&fit=crop&crop=face",
                imageUrl = "https://images.unsplash.com/photo-1426604966848-d7adac402bff?w=400&h=400&fit=crop",
                caption = "Spotted this amazing bird today during my morning walk! Nature never fails to surprise me 🐦✨ #birdwatching #nature #wildlife",
                timestamp = System.currentTimeMillis() - 18000000, // 5 hours ago
                category = listOf("Nature", "Bird", "Wildlife"),
                likeCount = 2100,
                commentCount = 28,
                shareCount = 8,
                location = "Central Park, New York",
                isLiked = true,
                isBookmarked = true,
                tags = listOf("birds", "wildlife", "centralpark"),
                aspectRatio = 1f
            ),
            Post(
                id = "3",
                userId = "user3",
                userName = "Mike Johnson",
                userImage = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                imageUrl = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=600&fit=crop",
                caption = "Street photography session in downtown! Love capturing the energy and rhythm of city life 🏙️📸 #streetphotography #urban #citylife",
                timestamp = System.currentTimeMillis() - 43200000, // 12 hours ago
                category = listOf("Street", "Photography", "Urban"),
                likeCount = 1850,
                commentCount = 67,
                shareCount = 23,
                location = "Downtown Los Angeles, CA",
                isLiked = false,
                isBookmarked = false,
                tags = listOf("street", "urban", "losangeles"),
                aspectRatio = 2f/3f
            )
        )

        _posts.value = dummyPosts
    }

    fun toggleLike(postId: String) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    isLiked = !post.isLiked,
                    likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                )
            } else post
        }
        _posts.value = updatedPosts
    }

    fun toggleBookmark(postId: String) {
        val updatedPosts = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isBookmarked = !post.isBookmarked)
            } else post
        }
        _posts.value = updatedPosts
    }
}