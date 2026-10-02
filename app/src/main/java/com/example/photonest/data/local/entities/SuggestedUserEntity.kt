package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggested_users")
data class SuggestedUserEntity(
    @PrimaryKey val userId: String,
    val fetchedAt: Long = System.currentTimeMillis()
)