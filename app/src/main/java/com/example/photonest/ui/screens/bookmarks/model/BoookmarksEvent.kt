package com.example.photonest.ui.screens.bookmarks.model

sealed class BookmarksEvent {
    object RefreshBookmarks : BookmarksEvent()
    object ToggleViewType : BookmarksEvent()
    data class ToggleBookmark(val postId: String) : BookmarksEvent()
    data class ToggleLike(val postId: String) : BookmarksEvent()
    object DismissErrorDialog : BookmarksEvent()
}
