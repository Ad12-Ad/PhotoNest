package com.example.photonest.data.mapper

import com.example.photonest.data.local.entities.PostEntity
import com.example.photonest.data.model.Post

fun Post.toEntity(): PostEntity {
    return PostEntity(
        id = id,
        userId = userId,
        userName = userName,
        userImage = userImage,
        imageUrl = imageUrl,
        caption = caption,
        timestamp = timestamp,
        category = category,
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        location = location,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        likedBy = likedBy,
        tags = tags,
        aspectRatio = aspectRatio,
        isEdited = isEdited,
        editedAt = editedAt
    )
}

fun PostEntity.toPost(): Post {
    return Post(
        id = id,
        userId = userId,
        userName = userName,
        userImage = userImage,
        imageUrl = imageUrl,
        caption = caption,
        timestamp = timestamp,
        category = category,
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        location = location,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        likedBy = likedBy,
        tags = tags,
        aspectRatio = aspectRatio,
        isEdited = isEdited,
        editedAt = editedAt
    )
}
