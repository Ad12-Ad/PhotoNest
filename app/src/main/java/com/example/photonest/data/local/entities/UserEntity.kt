package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val username: String,
    val profilePicture: String,
    val bio: String,
    val website: String,
    val location: String,
    val joinedDate: Long,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isVerified: Boolean,
    val isPrivate: Boolean,
    val bookmarks: List<String>,
    val following: List<String>,
    val followers: List<String>,
    val fcmToken: String,
    val lastSeen: Long,
    val isOnline: Boolean
)
