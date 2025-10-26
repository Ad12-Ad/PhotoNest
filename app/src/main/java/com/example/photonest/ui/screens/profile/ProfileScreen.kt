package com.example.photonest.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.data.model.User
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.UserListBottomSheet
import com.example.photonest.ui.components.UserListType
import com.example.photonest.ui.components.states.LoadingState
import com.example.photonest.ui.screens.profile.components.ProfileHeader
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettingScreen: () -> Unit = {},
    onNavToPersonalDetails: () -> Unit = {},
    onNavToBookmarkCollection: () -> Unit = {},
    onNavToLikedPosts: () -> Unit = {},
    onNavToYourPosts: () -> Unit = {},
    onNavToNotifications: () -> Unit = {},
    onNavToTheme: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onLogOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val followersSheetState = rememberModalBottomSheetState()
    val followingSheetState = rememberModalBottomSheetState()
    var showFollowersSheet by remember { mutableStateOf(false) }
    var showFollowingSheet by remember { mutableStateOf(false) }
    var followersList by remember { mutableStateOf<List<User>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingFollowers by remember { mutableStateOf(false) }
    var isLoadingFollowing by remember { mutableStateOf(false) }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    val uiState by viewModel.uiState.collectAsState()

    when{
        uiState.isLoading -> {
            LoadingState(modifier = Modifier.fillMaxSize())
        }
        uiState.error != null -> {
            MyAlertDialog(
                shouldShowDialog = uiState.showErrorDialog,
                onDismissRequest = {viewModel.dismissError()},
                title = "Can Load Profile",
                text = uiState.error ?: "An unknown error occurred",
                confirmButtonText = "Refresh",
                onConfirmClick = {viewModel.refreshProfile()}
            )
        }
        uiState.userProfile != null -> {
            Surface(color = MaterialTheme.colorScheme.surface) {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        uiState.userProfile?.let {
                            ProfileHeader(
                                user = it.user,
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
                    }
                    item {
                        ProfileSection(
                            title = "Account",
                            items = listOf(
                                ProfileSectionItem(
                                    icon = Icons.Outlined.Person,
                                    label = "Personal Details",
                                    onClick = onNavToPersonalDetails
                                ),
                                ProfileSectionItem(
                                    icon = Icons.Outlined.BookmarkBorder,
                                    label = "Bookmark Collection",
                                    onClick = onNavToBookmarkCollection
                                ),
                                ProfileSectionItem(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    label = "Liked Post",
                                    onClick = onNavToLikedPosts
                                ),
                                ProfileSectionItem(
                                    icon = Icons.Outlined.Lock,
                                    label = "Your Posts",
                                    onClick = onNavToYourPosts
                                )
                            )
                        )
                    }
                    item {
                        ProfileSection(
                            title = "Settings",
                            items = listOf(
                                ProfileSectionItem(
                                    icon = Icons.Outlined.Notifications,
                                    label = "Notifications",
                                    onClick = onNavToNotifications
                                ),
                                ProfileSectionItem(
                                    icon = Icons.Outlined.Palette,
                                    label = "Theme",
                                    onClick = onNavToTheme
                                ),
                                ProfileSectionItem(
                                    icon = Icons.Outlined.Settings,
                                    label = "More Settings",
                                    onClick = onNavigateToSettingScreen
                                )
                            )
                        )
                    }

                    item {
                        ProfileSection(
                            items = listOf(
                                ProfileSectionItem(
                                    icon = Icons.Outlined.ExitToApp,
                                    label = "Log Out",
                                    onClick = onLogOut
                                )
                            )
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
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
                    onNavigateToUserProfile(userId)},
                onFollowClick = { userId, isFollowing ->
                    viewModel.onFollowClickFromSheet(
                        userId = userId,
                        currentUserId = currentUserId,
                        listType = UserListType.FOLLOWERS,  // or FOLLOWING based on which sheet
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
                    onNavigateToUserProfile(userId)
                },
                onFollowClick = { userId, isFollowing ->
                    if (isFollowing) {
                        viewModel.unfollowUser(userId)
                    } else {
                        viewModel.followUser(userId)
                    }
                    isLoadingFollowing = true
                    viewModel.loadFollowing(uiState.userProfile?.user?.id ?: "") { users ->
                        followingList = users
                        isLoadingFollowing = false
                    }
                },
                currentUserId = it
            )
        }
    }
}



@Composable
fun StatIconLabel(value: Int, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        NormalText(
            text = "$value",
            fontSize = 20.sp
        )
        NormalText(
            text = label,
            fontSize = 14.sp
        )
    }
}

data class ProfileSectionItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun ProfileSection(
    title: String? = null,
    items: List<ProfileSectionItem>
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (title == null) 2.dp else 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            title?.let {
                NormalText(
                    text = it,
                    fontColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp)
                )
            }
            items.forEach { item ->
                ProfileSectionRow(item)
//                Divider()
            }
        }
    }
}

@Composable
fun ProfileSectionRow(item: ProfileSectionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        NormalText(
            text = item.label,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
