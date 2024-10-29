package com.example.photonest.ui.navigation

import com.example.photonest.R

sealed class BottomNavItem(
    val route: String,
    val iconSelected: Int,
    val iconNotSelected: Int,
    val title: String
) {
    object Home : BottomNavItem(AppDestinations.HOME_ROUTE, R.drawable.icon_home_filled,R.drawable.icon_home_filled, "Home")
    object Explore : BottomNavItem(AppDestinations.EXPLORE_ROUTE, R.drawable.icon_search_filled,R.drawable.icon_search_outlined, "Explore")
    object AddPost : BottomNavItem(AppDestinations.ADD_POST_ROUTE, R.drawable.icon_add,R.drawable.icon_add, "Add Post")
    object Bookmarks : BottomNavItem(AppDestinations.BOOKMARKS_ROUTE, R.drawable.bookmark_icon_filled,R.drawable.bookmark_icon_outlined, "Bookmarks")
    object Profile : BottomNavItem(AppDestinations.PROFILE_ROUTE, R.drawable.icon_profile_filled,R.drawable.icon_profile_outlined, "Profile")
}