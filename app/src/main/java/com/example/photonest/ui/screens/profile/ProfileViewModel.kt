package com.example.photonest.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.UserProfile
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile(userId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // If userId is null, get current user profile
                val targetUserId = userId ?: run {
                    val currentUserResult = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserResult == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "User not authenticated. Please sign in again.",
                                showErrorDialog = true
                            )
                        }
                        return@launch
                    }
                    currentUserResult
                }

                val result = userRepository.getUserProfile(targetUserId)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userProfile = result.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load profile",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
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

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val currentUserProfile = _uiState.value.userProfile ?: return@launch

            try {
                val result = if (currentUserProfile.isFollowing) {
                    userRepository.unfollowUser(userId)
                } else {
                    userRepository.followUser(userId)
                }

                when (result) {
                    is Resource.Success -> {
                        // Update UI state optimistically
                        val updatedProfile = currentUserProfile.copy(
                            isFollowing = !currentUserProfile.isFollowing,
                            user = currentUserProfile.user.copy(
                                followersCount = if (currentUserProfile.isFollowing) {
                                    currentUserProfile.user.followersCount - 1
                                } else {
                                    currentUserProfile.user.followersCount + 1
                                }
                            )
                        )
                        _uiState.update {
                            it.copy(userProfile = updatedProfile)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to update follow status",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "An error occurred",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(
                error = null,
                showErrorDialog = false
            )
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val showErrorDialog: Boolean = false
)
