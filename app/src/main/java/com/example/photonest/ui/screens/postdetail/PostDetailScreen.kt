package com.example.photonest.ui.screens.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.photonest.R
import com.example.photonest.data.model.Comment
import com.example.photonest.data.model.User
import com.example.photonest.ui.components.*
import com.example.photonest.ui.screens.home.components.PostItem
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val likesSheetState = rememberModalBottomSheetState()
    var showLikesSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var likesList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingLikes by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
        viewModel.loadUsersWhoLiked(postId) { users ->
            likesList = users
        }
    }

    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = viewModel::dismissError,
        title = "Error",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = viewModel::dismissError
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Post",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    BackCircleButton(onClick = onNavigateBack)
                },
                actions = {
                    if (uiState.postDetail?.post?.userId == currentUserId) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete Post")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.postDetail != null) {
                CommentInputBar(
                    userImage = uiState.currentUserImage,
                    comment = uiState.newComment,
                    onCommentChange = viewModel::updateComment,
                    onSendClick = viewModel::addComment,
                    isLoading = uiState.isAddingComment,
                    enabled = uiState.newComment.isNotBlank(),
                    onClearSearch = {viewModel.updateComment("")}
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.postDetail != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        PostItem(
                            post = uiState.postDetail!!.post,
                            onPostClick = { /* Already in detail view */ },
                            onLikeClick = { viewModel.toggleLike() },
                            onBookmarkClick = { viewModel.toggleBookmark() },
                            onCommentClick = { /* Already in comments view */ },
                            onShareClick = { viewModel.sharePost(context) },
                            onUserClick = { onNavigateToProfile(uiState.postDetail!!.post.userId) },
                            onFollowClick = { viewModel.toggleFollow() },
                            usersWhoLiked = likesList,
                            onLikesInfoClick = {
                                isLoadingLikes = true
                                showLikesSheet = true
                                viewModel.loadUsersWhoLiked(postId) { users ->
                                    likesList = users
                                    isLoadingLikes = false
                                }
                            },
                            shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
                        )
                    }
                    // Comments Section Header
                    item {
                        CommentsHeader(
                            commentCount = uiState.postDetail!!.comments.size
                        )
                    }

                    // Comments List
                    if (uiState.postDetail!!.comments.isEmpty()) {
                        item {
                            EmptyCommentsState()
                        }
                    } else {
                        items(
                            items = uiState.postDetail!!.comments,
                            key = { it.id }
                        ) { comment ->
                            EnhancedCommentItem(
                                comment = comment,
                                onUserClick = { onNavigateToProfile(comment.userId) },
                                onLikeClick = { /* TODO: Implement comment like */ },
                                onReplyClick = { /* TODO: Implement reply */ },
                                currentUserId = currentUserId,
                                onDeleteClick = if (comment.userId == currentUserId) {
                                    { viewModel.deleteComment(comment.id) }
                                } else null
                            )
                            Divider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Bottom spacing for input bar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showLikesSheet) {
        currentUserId?.let {
            UserListBottomSheet(
                sheetState = likesSheetState,
                userList = likesList,
                listType = UserListType.LIKES,
                isLoading = isLoadingLikes,
                onDismiss = { showLikesSheet = false },
                onUserClick = { userId ->
                    showLikesSheet = false
                    onNavigateToUserProfile(userId)
                },
                onFollowClick = { userId, isFollowing ->
                    viewModel.onFollowClickFromSheet(userId, isFollowing)
                },
                currentUserId = it
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = {
                Text("Are you sure you want to delete this post? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePost(postId) { success ->
                            if (success) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CommentsHeader(commentCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (commentCount == 1) "1 Comment" else "$commentCount Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun EmptyCommentsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.AddComment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No comments yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Be the first to comment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun EnhancedCommentItem(
    comment: Comment,
    onUserClick: () -> Unit,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // User Avatar
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(comment.userImage)
                .error(R.drawable.icon_profile_filled)
                .build(),
            contentDescription = "User avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onUserClick),
            loading = {
                ShimmerEffect(
                    modifier = Modifier.fillMaxSize()
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BrokenImage,
                        contentDescription = "Failed to load",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Comment Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            // Username and Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onUserClick)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatCommentTime(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentUserId == comment.userId) {
                    Spacer(modifier = Modifier.width(8.dp))

                    if (onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete comment",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Comment Text
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Like & Reply)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CommentActionButton(
                    text = "Like",
                    onClick = onLikeClick
                )

                CommentActionButton(
                    text = "Reply",
                    onClick = onReplyClick
                )
            }
        }
    }
}

@Composable
private fun CommentActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun CommentInputBar(
    userImage: String?,
    comment: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    onClearSearch: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User Avatar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userImage)
                    .crossfade(true)
                    .placeholder(R.drawable.icon_profile_outlined)
                    .error(R.drawable.icon_profile_outlined)
                    .build(),
                contentDescription = "Your avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            // Name Field
            OnBoardingTextField(
                value = comment,
                onValueChange = onCommentChange,
                showLabel = false,
                label = "Add a comment...",
                maxLines = 4,
                onClearSearch = onClearSearch,
                modifier = Modifier.weight(1f)
            )


            // Send Button
            IconButton(
                onClick = onSendClick,
                enabled = enabled && !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send comment",
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

// Helper function to format comment time (like "2h ago", "1d ago")
private fun formatCommentTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
