package com.example.photonest.ui.screens.home

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.photonest.data.model.User
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.UserListBottomSheet
import com.example.photonest.ui.components.UserListType
import com.example.photonest.ui.screens.home.components.PostItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var likesMap by remember { mutableStateOf<Map<String, List<User>>>(emptyMap()) }

    val likesSheetState = rememberModalBottomSheetState()
    var showLikesSheet by remember { mutableStateOf(false) }
    var selectedLikesList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingLikes by remember { mutableStateOf(false) }

    var currentPostIdForLikes by remember { mutableStateOf<String?>(null) }

    fun loadLikesForPost(postId: String) {
        if (likesMap[postId] != null) return

        viewModel.loadUsersWhoLiked(postId) { users ->
            likesMap = likesMap.toMutableMap().apply {
                put(postId, users)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh posts when returning to home screen
                viewModel.loadPosts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()


    val onRefresh: () -> Unit = {
        viewModel.refreshPosts()
    }

    // Error dialog
    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = { viewModel.dismissErrorDialog() },
        title = "Error",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = { viewModel.dismissErrorDialog() }
    )

    when {
        uiState.isLoading && uiState.posts.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.posts.isEmpty() -> {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                EmptyState(
                    onRefresh = onRefresh
                )
            }
        }

        else -> {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = modifier,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.posts,
                        key = { post -> post.id }
                    ) { post ->
                        LaunchedEffect(post.id) {
                            loadLikesForPost(post.id)
                        }

                        PostItem(
                            post = post,
                            onPostClick = { onPostClick(post.id) },
                            onUserClick = { onUserClick(post.userId) },
                            onLikeClick = { viewModel.toggleLike(post.id) },
                            onCommentClick = { onPostClick(post.id) },
                            onBookmarkClick = { viewModel.toggleBookmark(post.id) },
                            onShareClick = {
                                viewModel.sharePost(post, context)
                            },
                            onFollowClick = { viewModel.toggleFollow(post.userId, post.id) },
                            usersWhoLiked = likesMap[post.id] ?: emptyList(),
                            onLikesInfoClick = {
                                currentPostIdForLikes = post.id
                                isLoadingLikes = true
                                showLikesSheet = true
                                viewModel.loadUsersWhoLiked(post.id) { users ->
                                    selectedLikesList = users
                                    isLoadingLikes = false
                                }
                            }
                        )
                    }

                    if (uiState.isLoading && uiState.posts.isNotEmpty()) {
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

    if (showLikesSheet) {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            UserListBottomSheet(
                sheetState = likesSheetState,
                userList = selectedLikesList,
                listType = UserListType.LIKES,
                isLoading = isLoadingLikes,
                onDismiss = { showLikesSheet = false },
                onUserClick = { userId ->
                    showLikesSheet = false
                    onUserClick(userId)
                },
                onFollowClick = { userId, wasFollowing ->
                    currentPostIdForLikes?.let { postId ->
                        viewModel.toggleFollowFromBottomSheet(
                            userId = userId,
                            isCurrentlyFollowing = wasFollowing,
                            postId = postId
                        )

                        viewModel.loadUsersWhoLiked(postId) { refreshedList ->
                            selectedLikesList = refreshedList
                            likesMap = likesMap.toMutableMap().apply {
                                put(postId, refreshedList)
                            }
                        }
                    }
                },
                currentUserId = it
            )
        }
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