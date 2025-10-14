package com.example.photonest.ui.components

import java.text.SimpleDateFormat
import java.util.*

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"          // minutes ago
        diff < 86_400_000 -> "${diff / 3_600_000}h"      // hours ago
        diff < 604_800_000 -> "${diff / 86_400_000}d"    // days ago
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))  // Date string
    }
}
