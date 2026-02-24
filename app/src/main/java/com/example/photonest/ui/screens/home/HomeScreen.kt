package com.example.photonest.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.ui.components.UserListBottomSheet
import com.example.photonest.ui.components.UserListType
import com.example.photonest.ui.screens.home.components.PostItem
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.photonest.utils.ObserveAsEvents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val likesSheetState = rememberModalBottomSheetState()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is HomeEffect.NavigateToPost ->
                onPostClick(effect.postId)

            is HomeEffect.NavigateToUser ->
                onUserClick(effect.userId)

            is HomeEffect.Share -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share Post")
                )
            }

            is HomeEffect.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = modifier
    ) { padding ->

        when {
            state.isLoading && state.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.posts.isEmpty() -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        viewModel.onEvent(HomeEvent.Refresh)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    EmptyState(
                        onRefresh = {
                            viewModel.onEvent(HomeEvent.Refresh)
                        }
                    )
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        viewModel.onEvent(HomeEvent.Refresh)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.posts,
                            key = { it.id }
                        ) { post ->

                            PostItem(
                                post = post,
                                onPostClick = {
                                    viewModel.onEvent(
                                        HomeEvent.PostClicked(post.id)
                                    )
                                },
                                onUserClick = {
                                    viewModel.onEvent(
                                        HomeEvent.UserClicked(post.userId)
                                    )
                                },
                                onLikeClick = {
                                    viewModel.onEvent(
                                        HomeEvent.ToggleLike(post.id)
                                    )
                                },
                                onCommentClick = {
                                    viewModel.onEvent(
                                        HomeEvent.PostClicked(post.id)
                                    )
                                },
                                onBookmarkClick = {
                                    viewModel.onEvent(
                                        HomeEvent.ToggleBookmark(post.id)
                                    )
                                },
                                onShareClick = {
                                    viewModel.onEvent(
                                        HomeEvent.SharePost(post.id)
                                    )
                                },
                                onFollowClick = {
                                    viewModel.onEvent(
                                        HomeEvent.ToggleFollow(
                                            userId = post.userId,
                                            postId = post.id
                                        )
                                    )
                                },
                                usersWhoLiked = emptyList(), // populated via bottom sheet
                                onLikesInfoClick = {
                                    viewModel.onEvent(
                                        HomeEvent.OpenLikes(post.id)
                                    )
                                }
                            )
                        }

                        if (state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.isLikesSheetVisible) {
        UserListBottomSheet(
            sheetState = likesSheetState,
            userList = state.likedUsers,
            listType = UserListType.LIKES,
            isLoading = state.isLikesLoading,
            onDismiss = {
                viewModel.onEvent(HomeEvent.CloseLikes)
            },
            onUserClick = { userId ->
                viewModel.onEvent(HomeEvent.CloseLikes)
                onUserClick(userId)
            }
        )
    }
}

@Composable
private fun EmptyState(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No posts yet",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pull to refresh or check your connection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRefresh) {
            Text("Refresh")
        }
    }
}