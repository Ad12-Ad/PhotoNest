package com.example.photonest.feature.feed.postdetail

import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.photonest.R
import com.example.photonest.core.ui.components.BackCircleButton
import com.example.photonest.core.ui.components.OnBoardingTextField
import com.example.photonest.core.ui.components.ShimmerEffect
import com.example.photonest.core.ui.components.UserListBottomSheet
import com.example.photonest.core.ui.components.UserListType
import com.example.photonest.data.model.Comment
import com.example.photonest.ui.components.*
import com.example.photonest.feature.feed.home.components.PostItem
import com.example.photonest.core.utils.ObserveAsEvents
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val likesSheetState = rememberModalBottomSheetState()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect){
            PostDetailEffect.NavigateBack -> onNavigateBack()
            is PostDetailEffect.Share -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, effect.text)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share Post")
                )
            }
            is PostDetailEffect.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    LaunchedEffect(postId) {
        viewModel.onEvent(PostDetailEvent.Load(postId))
    }

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
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (state.postDetail != null) {
                CommentInputBar(
                    userImage = state.currentUserImage,
                    comment = state.newComment,
                    onCommentChange = {
                        viewModel.onEvent(
                            PostDetailEvent.UpdateComment(it)
                        )
                    },
                    onSendClick = {
                        viewModel.onEvent(PostDetailEvent.AddComment)
                    },
                    isLoading = state.isAddingComment,
                    enabled = state.newComment.isNotBlank(),
                    onClearSearch = {
                        viewModel.onEvent(
                            PostDetailEvent.UpdateComment("")
                        )
                    }
                )
            }
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.postDetail != null -> {
                val postDetail = state.postDetail!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    /* -------- Post -------- */

                    item {
                        PostItem(
                            post = postDetail.post,
                            onPostClick = {},
                            onLikeClick = {
                                viewModel.onEvent(PostDetailEvent.ToggleLike)
                            },
                            onBookmarkClick = {
                                viewModel.onEvent(PostDetailEvent.ToggleBookmark)
                            },
                            onCommentClick = {},
                            onShareClick = {
                                viewModel.onEvent(PostDetailEvent.SharePost)
                            },
                            onUserClick = {
                                onNavigateToProfile(postDetail.post.userId)
                            },
                            onFollowClick = {
                                viewModel.onEvent(PostDetailEvent.ToggleFollow)
                            },
                            usersWhoLiked = state.likedUsers,
                            onLikesInfoClick = {
                                viewModel.onEvent(PostDetailEvent.OpenLikesSheet)
                            },
                            shape = RoundedCornerShape(
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                    }

                    item {
                        CommentsHeader(
                            commentCount = postDetail.comments.size
                        )
                    }

                    if (postDetail.comments.isEmpty()) {
                        item { EmptyCommentsState() }
                    } else {
                        items(
                            items = postDetail.comments,
                            key = { it.id }
                        ) { comment ->
                            EnhancedCommentItem(
                                comment = comment,
                                currentUserId = state.currentUserId,
                                onUserClick = {
                                    onNavigateToProfile(comment.userId)
                                },
                                onDeleteClick =
                                    if (comment.userId == state.currentUserId)
                                    {
                                        {
                                            viewModel.onEvent(
                                                PostDetailEvent.DeleteComment(comment.id)
                                            )
                                        }
                                    }
                                    else null,
                                onLikeClick = {},
                                onReplyClick = {}
                            )
                            Divider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
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
                viewModel.onEvent(PostDetailEvent.CloseLikesSheet)
            },
            onUserClick = { userId ->
                viewModel.onEvent(PostDetailEvent.CloseLikesSheet)
                onNavigateToProfile(userId)
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
