package com.example.photonest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.photonest.R
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.navigation.AppDestinations
import com.example.photonest.ui.navigation.BottomNavItem
import com.example.photonest.ui.theme.PhotoNestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    navController: NavHostController,
    onAddPostClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = remember(currentRoute) {
        currentRoute in listOf(
            AppDestinations.HOME_ROUTE,
            AppDestinations.EXPLORE_ROUTE,
            AppDestinations.BOOKMARKS_ROUTE,
            AppDestinations.PROFILE_ROUTE
        )
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var isThemeChanging by remember { mutableStateOf(false) }

    PhotoNestTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (showBottomBar) {
                    TopAppBar(
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                        title = {
                            Image(
                                painter = painterResource(id = R.drawable.logo_text),
                                contentDescription = "Logo Text",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                            )
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Notifications Icon
                                NotificationIconButton(
                                    hasUnread = hasUnreadNotifications,
                                    onClick = onNotificationClick
                                )

                                // Enhanced Theme Toggle
                                ThemeToggleButton(
                                    isDarkTheme = isDarkTheme,
                                    isChanging = isThemeChanging,
                                    onToggle = {
                                        isThemeChanging = true
                                        onThemeToggle()
                                        // Reset animation after delay
                                        kotlinx.coroutines.GlobalScope.launch {
                                            delay(300)
                                            isThemeChanging = false
                                        }
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomAppBar(
                        currentRoute = currentRoute,
                        navController = navController,
                        onAddPostClick = onAddPostClick
                    )
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@Composable
private fun NotificationIconButton(
    hasUnread: Boolean,
    onClick: () -> Unit
) {
    Box {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = if (hasUnread) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                contentDescription = "Notifications",
                modifier = Modifier.size(20.dp)
            )
        }

        // Notification badge
        if (hasUnread) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .padding(2.dp),
                containerColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ThemeToggleButton(
    isDarkTheme: Boolean,
    isChanging: Boolean,
    onToggle: () -> Unit
) {
    FilledIconButton(
        onClick = onToggle,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isChanging) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            },
            contentColor = if (isChanging) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        ),
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
    ) {
        Icon(
            imageVector = if (isDarkTheme) {
                Icons.Default.LightMode
            } else {
                Icons.Default.DarkMode
            },
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BottomAppBar(
    currentRoute: String?,
    navController: NavHostController,
    onAddPostClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Explore,
            BottomNavItem.AddPost,
            BottomNavItem.Bookmarks,
            BottomNavItem.Profile
        )

        items.forEach { item ->
            val selected = currentRoute == item.route

            if (item == BottomNavItem.AddPost) {
                FloatingActionButton(
                    modifier = Modifier.padding(
                        bottom = 10.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    onClick = { onAddPostClick() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 8.dp
                    )
                ) {
                    Image(
                        painter = painterResource(id = item.iconSelected),
                        contentDescription = "Add Post",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.size(30.dp)
                    )
                }
            } else {
                NavigationBarItem(
                    modifier = Modifier.height(60.dp),
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (selected) item.iconSelected else item.iconNotSelected
                            ),
                            contentDescription = item.title,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        NormalText(
                            text = item.title,
                            fontSize = 12.sp,
                            fontColor = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}