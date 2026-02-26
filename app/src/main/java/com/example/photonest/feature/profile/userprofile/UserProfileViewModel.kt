package com.example.photonest.feature.profile.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.ToggleFollowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val postRepository: IPostRepository,
    private val toggleFollowUseCase: ToggleFollowUseCase
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
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userProfile = userResult.data,
                                    error = null
                                )
                            }
                            loadUserPosts(userId)
                        }
                        is NetworkResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = userResult.message ?: "Failed to load profile",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
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

    fun loadFollowers(userId: String, onResult: (List<User>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.getFollowers(userId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
                            onResult(result.data ?: emptyList())
                        }
                        is NetworkResult.Error -> {
                            onResult(emptyList())
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun loadFollowing(userId: String, onResult: (List<User>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.getFollowing(userId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
                            onResult(result.data ?: emptyList())
                        }
                        is NetworkResult.Error -> {
                            onResult(emptyList())
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
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
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(posts = postsResult.data ?: emptyList())
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { it.copy(posts = emptyList()) }
                        }
                        is NetworkResult.Loading -> { }
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
            val wasFollowing = currentUserProfile.isFollowing

            val updatedProfile = currentUserProfile.copy(
                isFollowing = !wasFollowing,
                user = currentUserProfile.user.copy(
                    followersCount = if (wasFollowing) {
                        currentUserProfile.user.followersCount - 1
                    } else {
                        currentUserProfile.user.followersCount + 1
                    }
                )
            )

            withContext(Dispatchers.Main){
                _uiState.update { it.copy(userProfile = updatedProfile) }
            }

            val result = toggleFollowUseCase(targetUserId = userId, isCurrentlyFollowing = wasFollowing)

            when (result) {
                is NetworkResult.Success -> { /* Already updated optimistically */ }
                is NetworkResult.Error -> {
                    withContext(Dispatchers.Main){
                        // Rollback to previous state
                        _uiState.update {
                            it.copy(
                                userProfile = currentUserProfile,
                                error = result.message ?: "Failed to update follow status",
                                showErrorDialog = true
                            )
                        }
                    }
                }
                is NetworkResult.Loading -> {}
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
