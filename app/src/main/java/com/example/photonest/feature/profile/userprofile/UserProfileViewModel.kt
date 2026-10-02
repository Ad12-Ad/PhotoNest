package com.example.photonest.feature.profile.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.User
import com.example.photonest.domain.model.UserProfile
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

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<UserProfileEffect>()
    val effect: SharedFlow<UserProfileEffect> = _effect.asSharedFlow()

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.LoadProfile -> loadProfileData(event.userId, isRefresh = false)
            is UserProfileEvent.RefreshProfile -> loadProfileData(event.userId, isRefresh = true)
            is UserProfileEvent.ToggleFollow -> toggleFollow(event.userId)

            is UserProfileEvent.OpenFollowersSheet -> loadFollowers(event.userId)
            is UserProfileEvent.OpenFollowingSheet -> loadFollowing(event.userId)
            UserProfileEvent.CloseSheets -> _state.update {
                it.copy(showFollowersSheet = false, showFollowingSheet = false)
            }

            is UserProfileEvent.PostClicked -> emitEffect(UserProfileEffect.NavigateToPost(event.postId))
            is UserProfileEvent.UserClicked -> {
                _state.update { it.copy(showFollowersSheet = false, showFollowingSheet = false) }
                emitEffect(UserProfileEffect.NavigateToUser(event.userId))
            }
            UserProfileEvent.BackClicked -> emitEffect(UserProfileEffect.NavigateBack)
        }
    }

    private fun loadProfileData(userId: String, isRefresh: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                if (isRefresh) it.copy(isRefreshing = true)
                else it.copy(isLoading = true, error = null)
            }

            val userResult = userRepository.getUserProfile(userId)
            val postsResult = postRepository.getUserPosts(userId)

            withContext(Dispatchers.Main) {
                if (userResult is NetworkResult.Success) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            userProfile = userResult.data,
                            posts = if (postsResult is NetworkResult.Success) postsResult.data ?: emptyList() else emptyList(),
                            error = null
                        )
                    }
                } else {
                    val errorMessage = userResult.message ?: "Failed to load profile"
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = errorMessage
                        )
                    }
                    emitEffect(UserProfileEffect.ShowError(errorMessage))
                }
            }
        }
    }

    private fun loadFollowers(userId: String) {
        _state.update { it.copy(showFollowersSheet = true, isLoadingFollowers = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.getFollowers(userId)
            withContext(Dispatchers.Main) {
                if (result is NetworkResult.Error) {
                    emitEffect(UserProfileEffect.ShowError(result.message ?: "Failed to load followers"))
                }
                _state.update {
                    it.copy(
                        isLoadingFollowers = false,
                        followersList = if (result is NetworkResult.Success) result.data ?: emptyList() else emptyList()
                    )
                }
            }
        }
    }

    private fun loadFollowing(userId: String) {
        _state.update { it.copy(showFollowingSheet = true, isLoadingFollowing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.getFollowing(userId)
            withContext(Dispatchers.Main) {
                if (result is NetworkResult.Error) {
                    emitEffect(UserProfileEffect.ShowError(result.message ?: "Failed to load following"))
                }
                _state.update {
                    it.copy(
                        isLoadingFollowing = false,
                        followingList = if (result is NetworkResult.Success) result.data ?: emptyList() else emptyList()
                    )
                }
            }
        }
    }

    private fun toggleFollow(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        val currentUserProfile = _state.value.userProfile ?: return@launch
        val wasFollowing = currentUserProfile.isFollowing

        // Optimistic UI Update
        val updatedProfile = currentUserProfile.copy(
            isFollowing = !wasFollowing,
            user = currentUserProfile.user.copy(
                followersCount = if (wasFollowing) currentUserProfile.user.followersCount - 1
                else currentUserProfile.user.followersCount + 1
            )
        )

        withContext(Dispatchers.Main) {
            _state.update { it.copy(userProfile = updatedProfile) }
        }

        val result = toggleFollowUseCase(targetUserId = userId, isCurrentlyFollowing = wasFollowing)

        if (result is NetworkResult.Error) {
            withContext(Dispatchers.Main) {
                _state.update { it.copy(userProfile = currentUserProfile) } // Rollback
                emitEffect(UserProfileEffect.ShowError(result.message ?: "Failed to update follow status"))
            }
        }
    }

    private fun emitEffect(effect: UserProfileEffect) = viewModelScope.launch { _effect.emit(effect) }
}