package com.example.photonest.feature.feed.postdetail.model

import com.example.photonest.domain.model.PostDetail
import com.example.photonest.domain.model.User

data class PostDetailState(
    val isLoading: Boolean = false,
    val postDetail: PostDetail? = null,

    // Likes bottom sheet
    val isLikesSheetVisible: Boolean = false,
    val isLikesLoading: Boolean = false,
    val likedUsers: List<User> = emptyList(),

    // Comment input
    val newComment: String = "",
    val isAddingComment: Boolean = false,

    // Current user
    val currentUserId: String? = null,
    val currentUserName: String? = null,
    val currentUserImage: String? = null,
)