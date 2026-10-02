package com.example.photonest.feature.feed.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.core.ui.components.UserListBottomSheet
import com.example.photonest.core.ui.components.UserListType
import com.example.photonest.feature.feed.home.components.PostItem
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.photonest.core.utils.ObserveAsEvents
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.model.User
import com.example.photonest.feature.explore.components.SuggestedUsersList
import com.example.photonest.feature.explore.components.TrendingPostsGrid
import com.example.photonest.feature.feed.home.model.HomeUiEffect
import com.example.photonest.feature.feed.home.model.HomeUiEvent

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
            is HomeUiEffect.NavigateToPost ->
                onPostClick(effect.postId)

            is HomeUiEffect.NavigateToUser ->
                onUserClick(effect.userId)

            is HomeUiEffect.Share -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share Post")
                )
            }

            is HomeUiEffect.ShowError -> {
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
                        viewModel.onEvent(HomeUiEvent.Refresh)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    EmptyState(
                        suggestedUsers = state.suggestedUsers,
                        trendingPosts = state.trendingPosts,
                        onUserClick = { userId -> viewModel.onEvent(HomeUiEvent.UserClicked(userId)) },
                        onPostClick = { postId -> viewModel.onEvent(HomeUiEvent.PostClicked(postId)) },
                        onFollowClick = { userId -> viewModel.onEvent(HomeUiEvent.FollowSuggestedUser(userId)) },
                    )
                }
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        viewModel.onEvent(HomeUiEvent.Refresh)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                    ) {
                        items(
                            items = state.posts,
                            key = { it.id }
                        ) { post ->

                            PostItem(
                                post = post,
                                onPostClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.PostClicked(post.id)
                                    )
                                },
                                onUserClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.UserClicked(post.userId)
                                    )
                                },
                                onLikeClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.ToggleLike(post.id)
                                    )
                                },
                                onCommentClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.PostClicked(post.id)
                                    )
                                },
                                onBookmarkClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.ToggleBookmark(post.id)
                                    )
                                },
                                onShareClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.SharePost(post.id)
                                    )
                                },
                                onFollowClick = {
                                    viewModel.onEvent(
                                        HomeUiEvent.ToggleFollow(
                                            userId = post.userId,
                                            postId = post.id
                                        )
                                    )
                                },
                                onViewLikesClicked = {
                                    viewModel.onEvent(
                                        HomeUiEvent.OpenLikes(post.id)
                                    )
                                },
                                modifier = Modifier.padding(8.dp)
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
                viewModel.onEvent(HomeUiEvent.CloseLikes)
            },
            onUserClick = { userId ->
                viewModel.onEvent(HomeUiEvent.CloseLikes)
                onUserClick(userId)
            }
        )
    }
}

@Composable
private fun EmptyState(
    suggestedUsers: List<User>,
    trendingPosts: List<Post>,
    onUserClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to PhotoNest!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Follow users to see their photos here in your feed. In the meantime, explore what's popular.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Suggested Users Section
        if (suggestedUsers.isNotEmpty()) {
            item {
                Text(
                    text = "Suggested Photographers",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                SuggestedUsersList(
                    users = suggestedUsers,
                    onUserClick = onUserClick,
                    onFollowClick = onFollowClick
                )
            }
        }

        // Trending Posts Section
        if (trendingPosts.isNotEmpty()) {
            item {
                Text(
                    text = "Trending Right Now",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                TrendingPostsGrid(
                    posts = trendingPosts,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    onPostClick = onPostClick,
                    modifier = Modifier.heightIn(max = 300.dp)
                )
            }
        }
    }
}