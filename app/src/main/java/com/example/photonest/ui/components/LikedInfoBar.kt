package com.example.photonest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.example.photonest.data.model.User

@Composable
fun LikesInfoBar(
    onClick: () -> Unit = {},
    users: List<User>,
    mainUsername: String,
    likeCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OverlappingProfileImages(users.take(5))

        Spacer(modifier = Modifier.width(16.dp))

        val likesText = buildAnnotatedString {
            append("Liked by ")
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )){
                append(mainUsername)
            }
            if (likeCount>1) {
                append(" and ")
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                ) {
                    append("${likeCount - 1} others")
                }
            }
        }

        Text(
            text = likesText,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun OverlappingProfileImages(users: List<User>, imageSize: Int = 28) {
    Box {
        users.forEachIndexed { index, user ->
            if (index < 2){
                SubcomposeAsyncImage(
                    model = user.profilePicture,
                    contentDescription = "${user.username} profile pic",
                    modifier = Modifier
                        .size(imageSize.dp)
                        .offset(x = (index * (imageSize/2)).dp) // overlap effect
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentScale = ContentScale.Crop,
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
            }
        }
    }
}
