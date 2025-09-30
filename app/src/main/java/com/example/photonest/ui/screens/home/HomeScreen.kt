package com.example.photonest.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.ui.screens.home.components.PostFeed

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    PostFeed(
        modifier = modifier,
        posts = uiState.posts,
        onPostClick = { /* Handle post click */ },
        onLikeClick = { post -> viewModel.toggleLike(post.id) },
        onBookmarkClick = { post -> viewModel.toggleBookmark(post.id) },
        onUserClick = { /* Handle user click */ }
    )
}