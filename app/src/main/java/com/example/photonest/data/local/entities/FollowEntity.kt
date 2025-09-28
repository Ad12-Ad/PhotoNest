package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey
    val id: String,
    val followerId: String,
    val followingId: String,
    val timestamp: Long,
    val isAccepted: Boolean
)