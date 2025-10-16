package com.example.photonest.ui.navigation

object AppDestinations {
    // Existing routes
    const val SPLASH_ROUTE = "splash"
    const val SIGN_UP_ROUTE = "sign_up"
    const val SIGN_IN_ROUTE = "sign_in"
    const val OTP_ROUTE = "otp"
    const val HOME_ROUTE = "home"

    // New bottom navigation routes
    const val EXPLORE_ROUTE = "explore"
    const val ADD_POST_ROUTE = "add_post"
    const val BOOKMARKS_ROUTE = "bookmarks"
    const val PROFILE_ROUTE = "profile"

    const val POST_DETAIL_ROUTE = "post_detail/{postId}"
    const val EDIT_PROFILE_ROUTE = "edit_profile"
    const val NOTIFICATION_ROUTE = "notifications"
    const val SETTINGS_ROUTE = "settings"
    const val FOLLOWERS_ROUTE = "followers/{userId}"
    const val FOLLOWING_ROUTE = "following/{userId}"

    const val NOTIFICATIONS_ROUTE = "notifications"
    const val LIKED_POSTS_ROUTE = "liked_posts"
    const val YOUR_POSTS_ROUTE = "your_posts"

    const val USER_PROFILE_ROUTE = "user_profile"
    fun getUserProfileRoute(userId: String) = "$USER_PROFILE_ROUTE/$userId"

    fun postDetailRoute(postId: String) = "post_detail/$postId"
    fun followersRoute(userId: String) = "followers/$userId"
    fun followingRoute(userId: String) = "following/$userId"
}