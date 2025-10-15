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
import com.example.photonest.ui.components.states.LoadingState
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.screens.explore.components.PostGridItem
import com.example.photonest.ui.screens.profile.components.UserProfileHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
    onFollowersClick: (String) -> Unit = {},
    onFollowingClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
}
