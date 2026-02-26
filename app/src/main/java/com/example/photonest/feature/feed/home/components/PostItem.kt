package com.example.photonest.feature.feed.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.photonest.R
import com.example.photonest.app.theme.PhotoNestTheme
import com.example.photonest.data.model.Post
import com.example.photonest.data.model.User
import com.example.photonest.core.ui.components.ShimmerEffect
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit = {},
    onViewLikesClicked: () -> Unit = {},
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    ) {
    val currentUserId = remember { try { FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null } }
    val isOwnPost = currentUserId == post.userId

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.userImage)
                    .crossfade(true)
                    .build(),
                contentDescription = post.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.clickable(onClick = onUserClick)
                    .size(40.dp)
                    .clip(CircleShape),
                loading = {
                    ShimmerEffect(
                        modifier = Modifier.fillMaxSize()
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .size(40.dp)
                            .clip(CircleShape)
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onUserClick() }
                    )

                    if (!isOwnPost) {
                        Text(
                            text = " • ",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            text = if (post.isUserFollowed) "Following" else "Follow",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (post.isUserFollowed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onFollowClick() }
                        )
                    }
                }

                if (post.location.isNotEmpty()) {
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { /* More Options */ }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 450.dp)
                    .clickable { onPostClick() },
                loading = { ShimmerEffect(Modifier.fillMaxSize()) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostActionButton(
                icon = if (post.isLiked) R.drawable.heart_icon else R.drawable.outlined_heart_icon,
                count = post.likeCount,
                tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                onClick = onLikeClick
            )

            PostActionButton(
                icon = R.drawable.icon_comment,
                count = post.commentCount,
                onClick = onCommentClick
            )

            PostActionButton(
                icon = R.drawable.icon_share_outlined,
                count = post.shareCount,
                onClick = onShareClick
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onBookmarkClick) {
                Icon(
                    painter = painterResource(
                        if (post.isBookmarked) R.drawable.bookmark_icon_filled else R.drawable.bookmark_icon_outlined
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (post.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val annotatedCaption = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${post.userName} ")
                }
                append(post.caption)
            }

            Text(
                text = annotatedCaption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "View all likes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onViewLikesClicked() }
            )
        }
    }
}

@Composable
private fun PostActionButton(
    icon: Int,
    count: Int,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- PREVIEW SECTION ---

@Preview(showBackground = true, name = "Modern Post Light Mode")
@Composable
fun PostItemPreview() {
    // Mock Data for Preview
    val mockPost = Post(
        id = "1",
        userId = "user_123",
        userName = "alex_design",
        userImage = "https://example.com/avatar.jpg",
        imageUrl = "https://example.com/post.jpg",
        caption = "Exploring the mountain peaks of the Alps! Such a breathtaking view. #travel #nature",
        location = "Swiss Alps, Switzerland",
        timestamp = System.currentTimeMillis(),
        likeCount = 1240,
        commentCount = 85,
        shareCount = 12,
        isLiked = true,
        isBookmarked = false,
        isUserFollowed = false,
        category = listOf("Travel", "Nature")
    )

    PhotoNestTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostItem(
                post = mockPost,
                onPostClick = {},
                onLikeClick = {},
                onBookmarkClick = {},
                onUserClick = {},
                onFollowClick = {},
                onViewLikesClicked = {}
            )
        }
    }
}