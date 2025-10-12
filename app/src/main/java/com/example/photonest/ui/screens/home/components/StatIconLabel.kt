package com.example.photonest.ui.screens.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.photonest.ui.components.NormalText

@Composable
fun StatIconLabel(
    onClick: ()-> Unit,
    @DrawableRes iconId: Int,
    label: Int,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.layoutId("bookmarkIcon")
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = "Stat Icon",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        if (label > 0) {
            NormalText(
                text = formatStatCount(label),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatStatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}