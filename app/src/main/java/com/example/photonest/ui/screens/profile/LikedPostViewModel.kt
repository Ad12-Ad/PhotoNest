package com.example.photonest.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.preferences.PreferencesManager
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedPostsViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _likedPosts = MutableStateFlow<List<Post>>(emptyList())
    val likedPosts: StateFlow<List<Post>> = _likedPosts.asStateFlow()

    init {
        loadLikedPosts()
    }

    private fun loadLikedPosts() {
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            val likedPostIds = userRepository.getLikedPostIdsByUserId(currentUserId)
            if (likedPostIds.isNotEmpty()) {
                val postsResource = postRepository.getPostsByIds(likedPostIds)
                if (postsResource is Resource.Success) {
                    _likedPosts.value = postsResource.data ?: emptyList()
                }
            } else {
                _likedPosts.value = emptyList()
            }
        }
    }
}

