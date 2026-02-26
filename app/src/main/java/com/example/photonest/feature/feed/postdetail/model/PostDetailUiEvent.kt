package com.example.photonest.feature.feed.postdetail.model

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
