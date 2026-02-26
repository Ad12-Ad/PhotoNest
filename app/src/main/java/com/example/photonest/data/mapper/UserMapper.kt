package com.example.photonest.data.mapper

import com.example.photonest.data.local.entities.UserEntity
import com.example.photonest.data.model.User

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        name = name,
        username = username,
        profilePicture = profilePicture,
        bio = bio,
        website = website,
        location = location,
        joinedDate = joinedDate,
        postsCount = postsCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isVerified = isVerified,
        isPrivate = isPrivate,
        fcmToken = fcmToken,
        lastSeen = lastSeen,
        isOnline = isOnline,
        birthday = birthday,
        onboardingCompleted = onboardingCompleted
    )
}

fun UserEntity.toUser(): User {
    return User(
        id = id,
        email = email,
        name = name,
        username = username,
        profilePicture = profilePicture,
        bio = bio,
        website = website,
        location = location,
        joinedDate = joinedDate,
        postsCount = postsCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isVerified = isVerified,
        isPrivate = isPrivate,
        fcmToken = fcmToken,
        birthday = birthday,
        onboardingCompleted = onboardingCompleted,
        lastSeen = lastSeen,
        isOnline = isOnline
    )
}
