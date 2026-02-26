package com.example.photonest.feature.bookmark.model

sealed class BookmarksEvent {
    object RefreshBookmarks : BookmarksEvent()
    object ToggleViewType : BookmarksEvent()
    data class ToggleBookmark(val postId: String) : BookmarksEvent()
    data class ToggleLike(val postId: String) : BookmarksEvent()
    object DismissErrorDialog : BookmarksEvent()
}
