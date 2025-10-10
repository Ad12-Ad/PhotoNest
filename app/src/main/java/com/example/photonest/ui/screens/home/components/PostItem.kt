package com.example.photonest.ui.screens.home.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.photonest.R
import com.example.photonest.data.model.Post
import com.example.photonest.ui.components.BackTxtBtn
import com.example.photonest.ui.components.FollowTxtBtn
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.annotatedText
import com.example.photonest.ui.theme.bodyFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostItem(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onUserClick: () -> Unit
) {
    val constraintSet = ConstraintSet {
        val userImage = createRefFor("userImage")
        val userName = createRefFor("userName")
        val timeStamp = createRefFor("timeStamp")
        val bookmarkIcon = createRefFor("bookmarkIcon")
        val followButton = createRefFor("followButton")
        val postCard = createRefFor("postCard")

        constrain(followButton) {
            start.linkTo(userName.end, 8.dp)
            centerVerticallyTo(userName)
        }

        constrain(userImage) {
            start.linkTo(parent.start, 16.dp)
            top.linkTo(parent.top)
        }
        constrain(userName) {
            start.linkTo(userImage.end, 12.dp)
            top.linkTo(parent.top)
        }
        constrain(timeStamp) {
            start.linkTo(userImage.end,12.dp)
            top.linkTo(userName.bottom,2.dp)
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.userImage)
                    .crossfade(true)
                    .build(),
                contentDescription = post.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clickable(onClick = onUserClick),
                placeholder = painterResource(R.drawable.profile_photo), // Add placeholder
                error = painterResource(R.drawable.p1) // Add error image
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

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateString = sdf.format(Date(post.timestamp))
        NormalText(
            text = dateString,
            fontSize = 12.sp,
            fontColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.layoutId("timeStamp")
        )

        FollowTxtBtn(
            onClick = {},
            modifier = Modifier.layoutId("followButton")
        )
        IconButton(
            onClick = onBookmarkClick,
            modifier = Modifier.layoutId("bookmarkIcon")
        ) {
            Icon(
                painter = painterResource(
                    id = if (post.isBookmarked) R.drawable.bookmark_icon_filled else R.drawable.bookmark_icon_outlined
                ),
                contentDescription = if (post.isBookmarked) "Bookmarked post" else "not a Bookmarked post",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        PostContentCard(
            userName = post.userName,
            imageUrl = post.imageUrl,
            categories = post.category,
            likeCount = post.likeCount,
            isLiked = post.isLiked,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            caption = post.caption,
            location = post.location,
            commentCount = post.commentCount,
            shareCount = post.shareCount,
            modifier = Modifier.layoutId("postCard")
        )
    }
}

@Composable
private fun PostContentCard(
    location: String,
    userName: String,
    imageUrl: String,
    categories: List<String>,
    likeCount: Int,
    commentCount: Int,
    shareCount: Int,
    isLiked: Boolean,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    caption: String,
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.profile_photo), // Add placeholder
            error = painterResource(R.drawable.p1), // Add error image
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 350.dp)
                .clickable(onClick = onPostClick)
        )

        Text(
            text = annotatedText(
                text1 = userName,
                text1Color = MaterialTheme.colorScheme.onSurfaceVariant,
                text2 = caption,
                text2Color = MaterialTheme.colorScheme.onBackground
            ),
            fontFamily = bodyFontFamily,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            if (location.isNotEmpty()){
                AssistChip(
                    enabled = false,
                    onClick = { },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.icon_location_outlined),
                            contentDescription = "location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    label = {
                        NormalText(
                            text = location,
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

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            StatIconLabel(
                onClick = onLikeClick,
                iconId = if (isLiked) R.drawable.heart_icon else R.drawable.outlined_heart_icon,
                label = likeCount,
                iconColor = if (isLiked) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatIconLabel(
                onClick = onCommentClick,
                iconId = R.drawable.icon_message_outlined,
                label = commentCount,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatIconLabel(
                onClick = onShareClick,
                iconId = R.drawable.icon_send_outlined,
                label = shareCount,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}