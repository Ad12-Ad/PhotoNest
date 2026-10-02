package com.example.photonest.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.photonest.R
import com.example.photonest.domain.model.User

enum class UserListType {
    FOLLOWERS,
    FOLLOWING,
    LIKES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListBottomSheet(
    sheetState: SheetState,
    userList: List<User>,
    listType: UserListType,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit,
    onSearchPerform: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }

    // Maintain a local copy of user list for instant recomposition and optimistic updates
    var localUserList by remember(userList) { mutableStateOf(userList) }

    LaunchedEffect(userList) {
        localUserList = userList
    }

    val filteredUsers = remember(localUserList, searchQuery) {
        if (searchQuery.isBlank()) localUserList
        else localUserList.filter {
            it.name.contains(searchQuery, true) || it.username.contains(searchQuery, true)
        }
    }

    val title = when (listType) {
        UserListType.FOLLOWERS -> "Followers"
        UserListType.FOLLOWING -> "Following"
        UserListType.LIKES -> "Liked by"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                if (localUserList.size > 5) {
                    OnBoardingTextField(
                        label = "Search here ...",
                        showLabel = false,
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        prefix = {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_search_outlined),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClearSearch = { searchQuery = "" },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        maxLines = 1,
                        onSearch = onSearchPerform,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 600.dp)
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                filteredUsers.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank()) "No users found" else "No results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->

                            UserListItem(
                                user = user,
                                onUserClick = { onUserClick(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    onUserClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = user.profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = { ShimmerEffect(Modifier.fillMaxSize()) },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.BrokenImage,
                            contentDescription = "Failed to load",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifEmpty { user.username },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (user.bio.isNotEmpty()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

//            if (!isCurrentUser) {
//                Spacer(modifier = Modifier.width(8.dp))
//
//                ButtonOnboarding(
//                    buttonText = if (isFollowing) "Following" else "Follow",
//                    onClick = onFollowClick,
//                    shape = RoundedCornerShape(20.dp),
//                    textColor = if (isFollowing) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onPrimary,
//                    textSize = 14.sp,
//                    textWeight = FontWeight.Medium,
//                    buttonColors = if (isFollowing)
//                        ButtonDefaults.outlinedButtonColors(
//                            containerColor = MaterialTheme.colorScheme.surface,
//                            contentColor = MaterialTheme.colorScheme.onSurface
//                        )
//                    else  {
//                        ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary,
//                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.4f)
//                        )
//                    },
//                    border = if (isFollowing)
//                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
//                    else null,
//                    modifier = Modifier.height(36.dp),
//                    elevation = if(isFollowing) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(4.dp)
//                )
//            }
        }
    }
}
