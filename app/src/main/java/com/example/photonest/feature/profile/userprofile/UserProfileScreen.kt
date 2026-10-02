package com.example.photonest.feature.profile.userprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.photonest.core.ui.components.UserListBottomSheet
import com.example.photonest.core.ui.components.UserListType
import com.example.photonest.core.utils.ObserveAsEvents
import com.example.photonest.feature.explore.components.PostGridItem
import com.example.photonest.feature.profile.components.UserProfileHeader
import com.example.photonest.ui.components.states.LoadingState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val followersSheetState = rememberModalBottomSheetState()
    val followingSheetState = rememberModalBottomSheetState()

    // 1. Initial Load
    LaunchedEffect(userId) {
        viewModel.onEvent(UserProfileEvent.LoadProfile(userId))
    }

    // 2. Observe One-Off Effects (Navigation & Snackbars)
    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is UserProfileEffect.NavigateBack -> onBackClick()
            is UserProfileEffect.NavigateToPost -> onPostClick(effect.postId)
            is UserProfileEffect.NavigateToUser -> onNavigateToUserProfile(effect.userId)
            is UserProfileEffect.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.userProfile?.user?.username ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(UserProfileEvent.BackClicked) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: More Options */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize().padding(paddingValues))
            }

            // Inline Error State (Replaces the Dialog)
            state.error != null && state.userProfile == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Couldn't Load Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "An unexpected error occurred.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.onEvent(UserProfileEvent.LoadProfile(userId)) }) {
                        Text("Try Again")
                    }
                }
            }

            state.userProfile != null -> {
                // 3. Pull to Refresh Box wrapping the content
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.onEvent(UserProfileEvent.RefreshProfile(userId)) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        item {
                            UserProfileHeader(
                                userProfile = state.userProfile!!,
                                isCurrentUser = state.userProfile!!.isCurrentUser,
                                onFollowClick = { viewModel.onEvent(UserProfileEvent.ToggleFollow(userId)) },
                                onFollowersClick = { viewModel.onEvent(UserProfileEvent.OpenFollowersSheet(userId)) },
                                onFollowingClick = { viewModel.onEvent(UserProfileEvent.OpenFollowingSheet(userId)) }
                            )
                        }

                        item {
                            Text(
                                text = "Posts (${state.posts.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        item {
                            if (state.posts.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No posts yet",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "This user hasn't shared any posts",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.height(400.dp) // Prevents nested scroll crash
                                ) {
                                    items(state.posts) { post ->
                                        PostGridItem(
                                            post = post,
                                            onClick = { viewModel.onEvent(UserProfileEvent.PostClicked(post.id)) }
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // 4. State-Driven Bottom Sheets
    if (state.showFollowersSheet) {
        UserListBottomSheet(
            sheetState = followersSheetState,
            userList = state.followersList,
            listType = UserListType.FOLLOWERS,
            isLoading = state.isLoadingFollowers,
            onDismiss = { viewModel.onEvent(UserProfileEvent.CloseSheets) },
            onUserClick = { clickedUserId -> viewModel.onEvent(UserProfileEvent.UserClicked(clickedUserId)) },
        )
    }

    if (state.showFollowingSheet) {
        UserListBottomSheet(
            sheetState = followingSheetState,
            userList = state.followingList,
            listType = UserListType.FOLLOWING,
            isLoading = state.isLoadingFollowing,
            onDismiss = { viewModel.onEvent(UserProfileEvent.CloseSheets) },
            onUserClick = { clickedUserId -> viewModel.onEvent(UserProfileEvent.UserClicked(clickedUserId)) }
        )
    }
}