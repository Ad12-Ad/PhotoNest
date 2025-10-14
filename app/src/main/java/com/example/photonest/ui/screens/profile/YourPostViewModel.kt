package com.example.photonest.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IPostRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YourPostsViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _yourPosts = MutableStateFlow<List<Post>>(value = emptyList())
    val yourPosts: StateFlow<List<Post>> = _yourPosts.asStateFlow()

    init {
        loadYourPosts()
    }

    private fun loadYourPosts() {
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            val postsResource = postRepository.getUserPosts(currentUserId)
            if (postsResource is Resource.Success) {
                _yourPosts.value = postsResource.data ?: emptyList()
            } else {
                _yourPosts.value = emptyList()
            }
        }
    }
}
