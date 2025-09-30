package com.example.photonest.ui.screens.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.example.photonest.R
import com.example.photonest.data.model.Post
import com.example.photonest.ui.components.NormalText

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val constraintSet = ConstraintSet {
        val userImage = createRefFor("userImage")
        val userName = createRefFor("userName")
        val timeStamp = createRefFor("timeStamp")
        val bookmarkIcon = createRefFor("bookmarkIcon")
        val postCard = createRefFor("postCard")

        constrain(userImage) {
            start.linkTo(parent.start, 16.dp)
            top.linkTo(parent.top)
        }
        constrain(userName) {
            start.linkTo(userImage.end, 12.dp)
            top.linkTo(userImage.top)
        }
        constrain(timeStamp) {
            start.linkTo(userImage.end,12.dp)
            top.linkTo(userName.bottom)
            bottom.linkTo(userImage.bottom)
            height = Dimension.fillToConstraints
        }
        constrain(bookmarkIcon) {
            end.linkTo(parent.end, 8.dp)
            centerVerticallyTo(userImage)
        }
        constrain(postCard) {
            top.linkTo(userImage.bottom, 12.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    }

    ConstraintLayout(
        modifier = modifier.fillMaxWidth(),
        constraintSet = constraintSet
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            modifier = Modifier
                .size(40.dp)
                .layoutId("userImage")
        ) {
            Image(
                painter = painterResource(post.userImage.toInt()),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clickable(onClick = onUserClick)
            )
        }


        NormalText(
            text = post.userName,
            fontWeight = FontWeight.SemiBold,
            fontColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .layoutId("userName")
                .clickable(onClick = onUserClick)
        )
        NormalText(
            text = post.timestamp.toString(),
            fontSize = 12.sp,
            fontColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.layoutId("timeStamp")
        )

        IconButton(
            onClick = onBookmarkClick,
            modifier = Modifier.layoutId("bookmarkIcon")
        ) {
            Icon(
                painter = painterResource(
                    id = if (post.isBookmarked) R.drawable.bookmark_icon_outlined else R.drawable.bookmark_icon_filled
                ),
                contentDescription = if (post.isBookmarked) "Bookmarked post" else "not a Bookmarked post",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        PostContentCard(
            image = post.imageUrl.toInt(),
            categories = post.category,
            likeCount = post.likeCount,
            isLiked = post.isLiked,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            modifier = Modifier.layoutId("postCard")
        )
    }
}

@Composable
private fun PostContentCard(
    @DrawableRes image: Int,
    categories: List<String>,
    likeCount: Int,
    isLiked: Boolean,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 350.dp),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.layoutId("bookmarkIcon")
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isLiked) R.drawable.heart_icon else R.drawable.outlined_heart_icon
                        ),
                        contentDescription = if (isLiked) "Unlike post" else "Like post",
                        tint = if (isLiked) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                NormalText(
                    text = formatLikeCount(likeCount),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        AssistChip(
                            enabled = false,
                            onClick = { },
                            label = {
                                NormalText(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatLikeCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
