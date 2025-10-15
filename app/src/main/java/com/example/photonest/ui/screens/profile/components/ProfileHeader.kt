package com.example.photonest.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.photonest.R
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.screens.profile.StatIconLabel

@Composable
fun ProfileHeader(user: User) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.profile_photo),
                error = painterResource(R.drawable.profile_photo)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                NormalText(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                NormalText(
                    text = "(" + user.email + ")",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatIconLabel(user.postsCount, "Posts")
                    StatIconLabel(user.followersCount, "Followers")
                    StatIconLabel(user.followingCount, "Following")
                }
            }
        }
    }
}

/*
* This is a backup file.
* Will be used in the case of creating the Profile detail page
*/
@Composable
fun ProfileHeader1(
    userProfile: UserProfile,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            AsyncImage(
                model = userProfile.user.profilePicture.ifEmpty {
                    "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=120&h=120&fit=crop&crop=face"
                },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.profile_photo),
                error = painterResource(id = R.drawable.profile_photo)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name and Username
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = userProfile.user.name.ifEmpty { userProfile.user.username },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (userProfile.user.isVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (userProfile.user.name.isNotEmpty() && userProfile.user.username.isNotEmpty()) {
            Text(
                text = "@${userProfile.user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Bio
        if (userProfile.user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = userProfile.user.bio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Location and Website
        if (userProfile.user.location.isNotEmpty() || userProfile.user.website.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userProfile.user.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userProfile.user.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (userProfile.user.location.isNotEmpty() && userProfile.user.website.isNotEmpty()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (userProfile.user.website.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            try {
                                val url = if (userProfile.user.website.startsWith("http")) {
                                    userProfile.user.website
                                } else {
                                    "https://${userProfile.user.website}"
                                }
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Website",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userProfile.user.website.removePrefix("https://").removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Follow Button (only show if not current user)
        if (!userProfile.isCurrentUser) {
            Spacer(modifier = Modifier.height(20.dp))

            if (userProfile.isFollowRequestPending) {
                OutlinedButton(
                    onClick = { /* Handle pending request */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text("Request Pending")
                }
            } else {
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (userProfile.isFollowing) {
                        ButtonDefaults.outlinedButtonColors()
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    border = if (userProfile.isFollowing) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Text(
                        text = if (userProfile.isFollowing) "Unfollow" else "Follow",
                        color = if (userProfile.isFollowing) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
        }

        // Mutual Followers (only show if not current user and has mutual followers)
        if (!userProfile.isCurrentUser && userProfile.mutualFollowersCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Followed by ${userProfile.mutualFollowersCount} people you follow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
