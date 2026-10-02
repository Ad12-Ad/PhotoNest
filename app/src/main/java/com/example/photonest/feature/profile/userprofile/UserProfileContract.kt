package com.example.photonest.feature.profile.userprofile

import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.User
import com.example.photonest.domain.model.UserProfile

data class UserProfileState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userProfile: UserProfile? = null,
    val posts: List<Post> = emptyList(),
    val error: String? = null,

    // Bottom Sheet States
    val showFollowersSheet: Boolean = false,
    val showFollowingSheet: Boolean = false,
    val followersList: List<User> = emptyList(),
    val followingList: List<User> = emptyList(),
    val isLoadingFollowers: Boolean = false,
    val isLoadingFollowing: Boolean = false
)

sealed interface UserProfileEvent {
    data class LoadProfile(val userId: String) : UserProfileEvent
    data class RefreshProfile(val userId: String) : UserProfileEvent
    data class ToggleFollow(val userId: String) : UserProfileEvent

    data class OpenFollowersSheet(val userId: String) : UserProfileEvent
    data class OpenFollowingSheet(val userId: String) : UserProfileEvent
    data object CloseSheets : UserProfileEvent

    data class PostClicked(val postId: String) : UserProfileEvent
    data class UserClicked(val userId: String) : UserProfileEvent
    data object BackClicked : UserProfileEvent
}

sealed interface UserProfileEffect {
    data class ShowError(val message: String) : UserProfileEffect
    data class NavigateToPost(val postId: String) : UserProfileEffect
    data class NavigateToUser(val userId: String) : UserProfileEffect
    data object NavigateBack : UserProfileEffect
}