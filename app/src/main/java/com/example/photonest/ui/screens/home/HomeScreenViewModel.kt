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
                userName = "John Doe",
                userImage = R.drawable.profile_photo,
                imageUrl = R.drawable.p1,
                timestamp = "2h ago",
                category = listOf("Nature", "Photography"),
                likeCount = 3300,
                isLiked = false,
                isBookmarked = false
            ),
            Post(
                id = "2",
                userName = "Jane Smith",
                userImage = R.drawable.profile_photo,
                imageUrl = R.drawable.p3,
                timestamp = "5h ago",
                category = listOf("Nature","Bird"),
                likeCount = 2100,
                isLiked = false,
                isBookmarked = false
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