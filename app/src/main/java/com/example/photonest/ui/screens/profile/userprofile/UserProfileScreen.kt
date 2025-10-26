package com.example.photonest.ui.screens.profile.userprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.data.model.User
import com.example.photonest.ui.components.states.LoadingState
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.UserListBottomSheet
import com.example.photonest.ui.components.UserListType
import com.example.photonest.ui.screens.explore.components.PostGridItem
import com.example.photonest.ui.screens.profile.components.UserProfileHeader
import com.google.firebase.auth.FirebaseAuth

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
    val uiState by viewModel.uiState.collectAsState()

    val followersSheetState = rememberModalBottomSheetState()
    val followingSheetState = rememberModalBottomSheetState()
    var showFollowersSheet by remember { mutableStateOf(false) }
    var showFollowingSheet by remember { mutableStateOf(false) }
    var followersList by remember { mutableStateOf<List<User>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingFollowers by remember { mutableStateOf(false) }
    var isLoadingFollowing by remember { mutableStateOf(false) }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    val profileUserId = uiState.userProfile?.user?.id ?: userId

    // Load user profile when screen opens
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.userProfile?.user?.username ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show options menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }

            uiState.error != null -> {
                MyAlertDialog(
                    shouldShowDialog = uiState.showErrorDialog,
                    onDismissRequest = { viewModel.dismissError() },
                    title = "Error Loading Profile",
                    text = uiState.error ?: "An unknown error occurred",
                    confirmButtonText = "Retry",
                    onConfirmClick = { viewModel.loadUserProfile(userId) }
                )
            }

            uiState.userProfile != null -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Profile header with follow button
                    item {
                        UserProfileHeader(
                            userProfile = uiState.userProfile!!,
                            isCurrentUser = uiState.userProfile!!.isCurrentUser,
                            onFollowClick = {
                                viewModel.toggleFollow(userId)
                            },
                            onFollowersClick = {
                                isLoadingFollowers = true
                                showFollowersSheet = true
                                viewModel.loadFollowers(uiState.userProfile?.user?.id ?: "") { users ->
                                    followersList = users
                                    isLoadingFollowers = false
                                }
                            },
                            onFollowingClick = {
                                isLoadingFollowing = true
                                showFollowingSheet = true
                                viewModel.loadFollowing(uiState.userProfile?.user?.id ?: "") { users ->
                                    followingList = users
                                    isLoadingFollowing = false
                                }
                            }
                        )
                    }

                    // Posts section header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Posts (${uiState.posts.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Posts grid
                    item {
                        if (uiState.posts.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
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
                                modifier = Modifier.height(400.dp) // Fixed height for nested scrolling
                            ) {
                                items(uiState.posts) { post ->
                                    PostGridItem(
                                        post = post,
                                        onClick = { onPostClick(post.id) }
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

    if (showFollowersSheet) {
        currentUserId?.let {
            UserListBottomSheet(
                sheetState = followersSheetState,
                userList = followersList,
                listType = UserListType.FOLLOWERS,
                isLoading = isLoadingFollowers,
                onDismiss = { showFollowersSheet = false },
                onUserClick = { userId ->
                    showFollowersSheet = false
                    onNavigateToUserProfile(userId)            },
                onFollowClick = { clickedUserId, isFollowing ->
                    viewModel.onFollowClickFromSheet(
                        userId = clickedUserId,
                        currentProfileUserId = profileUserId,
                        listType = "FOLLOWERS",
                        isCurrentlyFollowing = isFollowing
                    )
                },
                currentUserId = it
            )
        }
    }
    if (showFollowingSheet) {
        currentUserId?.let {
            UserListBottomSheet(
                sheetState = followingSheetState,
                userList = followingList,
                listType = UserListType.FOLLOWING,
                isLoading = isLoadingFollowing,
                onDismiss = { showFollowingSheet = false },
                onUserClick = { userId ->
                    showFollowingSheet = false
                    onNavigateToUserProfile(userId) // ⭐ USE CALLBACK
                },
                onFollowClick = { clickedUserId, isFollowing ->
                    viewModel.onFollowClickFromSheet(
                        userId = clickedUserId,
                        currentProfileUserId = profileUserId,
                        listType = "FOLLOWING",
                        isCurrentlyFollowing = isFollowing
                    )
                },
                currentUserId = it
            )
        }
    }
}
