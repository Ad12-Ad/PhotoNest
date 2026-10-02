package com.example.photonest.feature.profile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.IPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class YourPostsViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _yourPosts = MutableStateFlow<List<Post>>(emptyList())
    val yourPosts: StateFlow<List<Post>> = _yourPosts.asStateFlow()

    init { loadYourPosts() }

    private fun loadYourPosts() {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            val result = withContext(Dispatchers.IO) {
                postRepository.getUserPosts(currentUserId)
            }
            if (result is NetworkResult.Success) {
                _yourPosts.value = result.data ?: emptyList()
            }
        }
    }
}