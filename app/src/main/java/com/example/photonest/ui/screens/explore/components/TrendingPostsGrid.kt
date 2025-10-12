package com.example.photonest.ui.screens.explore.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.photonest.R
import com.example.photonest.data.model.Post
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TrendingPostsGrid(
    posts: List<Post>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true,
        contentPadding = PaddingValues(0.dp)
    ) {
        item {
            Spacer(Modifier.width(4.dp))
        }
        items(posts.take(6)) { post -> // Show only top 6 trending posts
            TrendingPostItem(
                post = post,
                onClick = { onPostClick(post.id) }
            )
        }
        item {
            Spacer(Modifier.width(4.dp))
        }
    }
}

// In TrendingPostsGrid.kt - Export just the item
@Composable
fun TrendingPostItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.caption,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.6f)),
                                startY = size.height * 0.7f,
                                endY = size.height
                            )
                        )
                    },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.p1),
                error = painterResource(id = R.drawable.p1)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.heart_icon),
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatCount(post.likeCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

//            Row(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userImage,
                    contentDescription = post.userName,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(50)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.profile_photo),
                    error = painterResource(id = R.drawable.profile_photo)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = post.userName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
        else -> NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }
}
