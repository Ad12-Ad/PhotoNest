package com.example.photonest.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val title: String
) {
    object Home : BottomNavItem(AppDestinations.HOME_ROUTE, Icons.Filled.Home,Icons.Outlined.Home, "Home")
    object Explore : BottomNavItem(AppDestinations.EXPLORE_ROUTE, Icons.Filled.Search,Icons.Outlined.Search, "Explore")
    object AddPost : BottomNavItem(AppDestinations.ADD_POST_ROUTE, Icons.Filled.Add,Icons.Outlined.Add, "Add Post")
    object Bookmarks : BottomNavItem(AppDestinations.BOOKMARKS_ROUTE, Icons.Filled.Bookmark,Icons.Outlined.BookmarkBorder, "Bookmarks")
    object Profile : BottomNavItem(AppDestinations.PROFILE_ROUTE, Icons.Filled.Person,Icons.Outlined.Person, "Profile")
}