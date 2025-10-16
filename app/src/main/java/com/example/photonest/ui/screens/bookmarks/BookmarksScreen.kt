package com.example.photonest.ui.screens.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.data.model.Post
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.IconType
import com.example.photonest.ui.components.PostGridItem
import com.example.photonest.ui.screens.bookmarks.model.BookmarksEvent
import com.example.photonest.ui.screens.home.components.PostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onPostClick: (String) -> Unit = {},
    viewModel: BookmarksViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle error dialog
    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = { viewModel.onEvent(BookmarksEvent.DismissErrorDialog) },
        title = "Error",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = { viewModel.onEvent(BookmarksEvent.DismissErrorDialog) }
    )

    Column(
        modifier = modifier
    ) {
        // Header with title and view toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bookmarks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = { viewModel.onEvent(BookmarksEvent.ToggleViewType) }
                ) {
                    Icon(
                        imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (uiState.isGridView) "List view" else "Grid view"
                    )
                }
            }
        }


        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.bookmarkedPosts.isEmpty() -> {
                EmptyBookmarksContent()
            }

            else -> {
                BookmarkedPostsContent(
                    posts = uiState.bookmarkedPosts,
                    isGridView = uiState.isGridView,
                    onPostClick = onPostClick,
                    onBookmarkToggle = { postId ->
                        viewModel.onEvent(BookmarksEvent.ToggleBookmark(postId))
                    },
                    onLikeToggle = { postId ->
                        viewModel.onEvent(BookmarksEvent.ToggleLike(postId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun EmptyBookmarksContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No bookmarks yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start bookmarking posts you want to save for later",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BookmarkedPostsContent(
    posts: List<Post>,
    isGridView: Boolean,
    onPostClick: (String) -> Unit,
    onBookmarkToggle: (String) -> Unit,
    onLikeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isGridView) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = modifier
        ) {
            items(posts) { post ->
                PostGridItem(
                    post = post,
                    onPostClick = { onPostClick(post.id) },
                    iconType = IconType.BOOKMARK,
                    onIconClick = { onBookmarkToggle(post.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(posts) { post ->
                PostItem(
                    post = post,
                    onPostClick = { onPostClick(post.id) },
                    onLikeClick = { onLikeToggle(post.id) },
                    onCommentClick = { onPostClick(post.id) },
                    onBookmarkClick = { onBookmarkToggle(post.id) },
                    onShareClick = { /* Handle share */ },
                    onUserClick = { /* Handle user click */ }
                )
            }
        }
    }
}
