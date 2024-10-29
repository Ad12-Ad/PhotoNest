package com.example.photonest.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.ui.screens.home.components.PostFeed

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    PostFeed(
        modifier = modifier,
        posts = posts,
        onPostClick = { /* Handle post click */ },
        onLikeClick = { post -> viewModel.toggleLike(post.id) },
        onBookmarkClick = { post -> viewModel.toggleBookmark(post.id) },
        onUserClick = { /* Handle user click */ }
    )
}