package com.example.photonest.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.photonest.R
import com.example.photonest.data.model.User
import com.example.photonest.data.model.UserProfile
import com.example.photonest.ui.components.ButtonOnboarding
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.ShimmerEffect
import com.example.photonest.ui.screens.profile.StatIconLabel

@Composable
fun ProfileHeader(
    user: User,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
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
            SubcomposeAsyncImage(
                model = user.profilePicture,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
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
                    StatIconLabel(user.followersCount, "Followers", onClick = onFollowersClick)
                    StatIconLabel(user.followingCount, "Following", onClick = onFollowingClick)
                }
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    userProfile: UserProfile,
    isCurrentUser: Boolean = false,
    onFollowClick: () -> Unit = {},
    onFollowersClick: (String) -> Unit = {},
    onFollowingClick: (String) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
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
            SubcomposeAsyncImage(
                model = userProfile.user.profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
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

        // Stats Row (Posts, Followers, Following)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${userProfile.user.postsCount}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickableIf(!isCurrentUser) { onFollowersClick(userProfile.user.id) }
            ) {
                Text(
                    text = "${userProfile.user.followersCount}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickableIf(!isCurrentUser) { onFollowingClick(userProfile.user.id) }
            ) {
                Text(
                    text = "${userProfile.user.followingCount}",  // ✅ FIXED: Use count field
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Following",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Bio
        if (userProfile.user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = userProfile.user.bio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
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

        // Action Button (Follow/Edit Profile)
        Spacer(modifier = Modifier.height(20.dp))

        if (isCurrentUser) {
            // Edit Profile Button
            ButtonOnboarding(
                buttonText = "Edit Profile",
                textSize = 20.sp,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                onClick = onEditProfileClick,
                buttonColors = ButtonDefaults.outlinedButtonColors(),
                enabled = false
            )
        } else {
            // Follow/Unfollow Button
            if (userProfile.isFollowRequestPending) {
                ButtonOnboarding(
                    buttonText = "Request Pending",
                    textSize = 20.sp,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    onClick = {},
                    enabled = false
                )
            } else {
                ButtonOnboarding(
                    buttonText = if (userProfile.isFollowing) "Following" else "Follow",
                    textSize = 20.sp,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    onClick = onFollowClick,
                    textColor = if (userProfile.isFollowing) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onPrimary,
                    buttonColors = if (userProfile.isFollowing) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.4f)
                        )
                    },
                    border = if (userProfile.isFollowing) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else null,
                    elevation = if(userProfile.isFollowing) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(4.dp)
                )
            }
        }

        // Mutual Followers (only show if not current user and has mutual followers)
        if (!isCurrentUser && userProfile.mutualFollowersCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Followed by ${userProfile.mutualFollowersCount} people you follow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
// Helper extension for conditional clickable
@Composable
fun Modifier.clickableIf(condition: Boolean, onClick: () -> Unit): Modifier {
    return if (condition) {
        this.clickable { onClick() }
    } else {
        this
    }
}