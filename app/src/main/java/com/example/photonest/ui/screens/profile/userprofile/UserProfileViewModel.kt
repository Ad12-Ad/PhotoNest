package com.example.photonest.ui.screens.profile.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.UserProfile
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val userResult = userRepository.getUserProfile(userId)

                withContext(Dispatchers.Main) {
                    when (userResult) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userProfile = userResult.data,
                                    error = null
                                )
                            }
                            loadUserPosts(userId)
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = userResult.message ?: "Failed to load profile",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred",
                            showErrorDialog = true
                        )
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val postsResult = postRepository.getUserPosts(userId)

                withContext(Dispatchers.Main) {
                    when (postsResult) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(posts = postsResult.data ?: emptyList())
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(posts = emptyList()) }
                        }
                        is Resource.Loading -> { }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(posts = emptyList()) }
                }
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserProfile = _uiState.value.userProfile ?: return@launch
            try {
                val wasFollowing = currentUserProfile.isFollowing
                val updatedProfile = currentUserProfile.copy(
                    isFollowing = !wasFollowing,
                    user = currentUserProfile.user.copy(
                        followersCount = if (wasFollowing)
                            currentUserProfile.user.followersCount - 1
                        else
                            currentUserProfile.user.followersCount + 1
                    )
                )

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(userProfile = updatedProfile) }
                }

                val result = if (wasFollowing) {
                    userRepository.unfollowUser(userId)
                } else {
                    userRepository.followUser(userId)
                }

                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> { }
                        is Resource.Error -> {
                            _uiState.update { it.copy(userProfile = currentUserProfile) }
                            _uiState.update {
                                it.copy(
                                    error = result.message ?: "Failed to update follow status",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is Resource.Loading -> { }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(userProfile = currentUserProfile) }
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "An error occurred",
                            showErrorDialog = true
                        )
                    }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, showErrorDialog = false) }
    }
}

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val showErrorDialog: Boolean = false
)
