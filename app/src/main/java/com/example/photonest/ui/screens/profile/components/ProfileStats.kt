package com.example.photonest.ui.screens.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.photonest.data.model.UserProfile
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileStats(
    userProfile: UserProfile,
    onPostsClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileStatItem(
            count = userProfile.user.postsCount,
            label = "Posts",
            onClick = onPostsClick,
            modifier = Modifier.weight(1f)
        )

        ProfileStatItem(
            count = userProfile.user.followersCount,
            label = "Followers",
            onClick = onFollowersClick,
            modifier = Modifier.weight(1f)
        )

        ProfileStatItem(
            count = userProfile.user.followingCount,
            label = "Following",
            onClick = onFollowingClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStatItem(
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val millions = count / 1_000_000.0
            if (millions == millions.toInt().toDouble()) {
                "${millions.toInt()}M"
            } else {
                String.format(Locale.getDefault(), "%.1fM", millions)
            }
        }
        count >= 1_000 -> {
            val thousands = count / 1_000.0
            if (thousands == thousands.toInt().toDouble()) {
                "${thousands.toInt()}K"
            } else {
                String.format(Locale.getDefault(), "%.1fK", thousands)
            }
        }
        else -> NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }
}
