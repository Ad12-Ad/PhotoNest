package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trending_feed")
data class TrendingFeedEntity(
    @PrimaryKey val postId: String,
    val fetchedAt: Long = System.currentTimeMillis()
)