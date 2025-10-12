package com.example.photonest.ui.screens.bookmarks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.photonest.R
import com.example.photonest.data.model.Post

@Composable
fun PostGridItem(
    post: Post,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.FillWidth
                )

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (post.isBookmarked) R.drawable.bookmark_icon_filled else R.drawable.bookmark_icon_outlined
                        ),
                        contentDescription = if (post.isBookmarked) "Bookmarked post" else "not a Bookmarked post",
                        tint = if (post.isBookmarked)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

//                // Bookmark icon
//                IconButton(
//                    onClick = onBookmarkClick,
//                    modifier = Modifier.align(Alignment.TopEnd)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Bookmark,
//                        contentDescription = "Bookmark",
//                        tint = if (post.isBookmarked)
//                            MaterialTheme.colorScheme.primary
//                        else
//                            MaterialTheme.colorScheme.outline
//                    )
//                }
            }

//            // Content section
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(12.dp)
//            ) {
//                Text(
//                    text = post.caption,
//                    style = MaterialTheme.typography.bodyMedium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                if (post.location.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = post.location,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.outline,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "${post.likeCount} likes",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.outline
//                    )
//
//                    Text(
//                        text = "${post.commentCount} comments",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.outline
//                    )
//                }
//            }
        }
    }
}
