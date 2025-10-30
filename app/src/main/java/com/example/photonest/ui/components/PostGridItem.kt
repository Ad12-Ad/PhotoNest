package com.example.photonest.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.photonest.R
import com.example.photonest.data.model.Post

enum class IconType {
    LIKE, BOOKMARK, NONE
}

@Composable
fun PostGridItem(
    post: Post,
    onPostClick: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconType: IconType = IconType.NONE,
) {
    Card(
        modifier = modifier
            .clickable { onPostClick() },
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

                if (iconType != IconType.NONE){
                    IconButton(
                        shape = CircleShape,
                        onClick = onIconClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if(iconType == IconType.LIKE) MaterialTheme.colorScheme.errorContainer.copy(0.6f) else MaterialTheme.colorScheme.surfaceContainer.copy(0.6f)
                        ),
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(25.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (iconType == IconType.BOOKMARK) R.drawable.bookmark_icon_filled else R.drawable.heart_icon
                            ),
                            contentDescription = if (iconType == IconType.BOOKMARK) "Bookmarked Icon" else "Heart Icon",
                            tint = if (iconType == IconType.BOOKMARK)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
