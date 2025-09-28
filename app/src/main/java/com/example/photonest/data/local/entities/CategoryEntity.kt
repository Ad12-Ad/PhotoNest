package com.example.photonest.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String,
    val color: String,
    val postsCount: Int,
    val isPopular: Boolean,
    val createdAt: Long
)
