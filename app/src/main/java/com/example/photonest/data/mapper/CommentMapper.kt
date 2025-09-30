package com.example.photonest.data.mapper

import com.example.photonest.data.local.entities.CommentEntity
import com.example.photonest.data.model.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun Comment.toEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        postId = postId,
        userId = userId,
        userName = userName,
        userImage = userImage,
        text = text,
        timestamp = timestamp,
        likeCount = likeCount,
        replyCount = replyCount,
        parentCommentId = parentCommentId,
        isLiked = isLiked,
        isOwner = isOwner,
        likedBy = gson.toJson(likedBy),
        mentions = gson.toJson(mentions),
        isEdited = isEdited,
        editedAt = editedAt
    )
}

fun CommentEntity.toComment(): Comment {
    val likedByType = object : TypeToken<List<String>>() {}.type
    val mentionsType = object : TypeToken<List<String>>() {}.type

    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        userName = userName,
        userImage = userImage,
        text = text,
        timestamp = timestamp,
        likeCount = likeCount,
        replyCount = replyCount,
        parentCommentId = parentCommentId,
        isLiked = isLiked,
        isOwner = isOwner,
        likedBy = try { gson.fromJson(likedBy, likedByType) } catch (e: Exception) { emptyList() },
        mentions = try { gson.fromJson(mentions, mentionsType) } catch (e: Exception) { emptyList() },
        isEdited = isEdited,
        editedAt = editedAt
    )
}
