package com.example.photonest.feature.feed.postdetail

import com.example.photonest.data.model.PostDetail
import com.example.photonest.data.model.User

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

sealed interface PostDetailEvent {

    data class Load(val postId: String) : PostDetailEvent

    // Post actions
    data object ToggleLike : PostDetailEvent
    data object ToggleBookmark : PostDetailEvent
    data object ToggleFollow : PostDetailEvent

    // Likes sheet
    data object OpenLikesSheet : PostDetailEvent
    data object CloseLikesSheet : PostDetailEvent

    // Comments
    data class UpdateComment(val value: String) : PostDetailEvent
    data object AddComment : PostDetailEvent
    data class DeleteComment(val commentId: String) : PostDetailEvent

    // Other
    data object SharePost : PostDetailEvent
    data object DismissError : PostDetailEvent
}

sealed interface PostDetailEffect {

    data class Share(val text: String) : PostDetailEffect
    data class ShowError(val message: String) : PostDetailEffect
    data object NavigateBack : PostDetailEffect
}
