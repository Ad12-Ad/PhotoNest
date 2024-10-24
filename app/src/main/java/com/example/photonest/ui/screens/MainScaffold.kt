package com.example.photonest.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.navigation.AppDestinations
import com.example.photonest.ui.navigation.BottomNavItem
import com.example.photonest.ui.theme.PhotoNestTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    navController: NavHostController,
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
    PhotoNestTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                if (showBottomBar) {
                    TopAppBar(
                        title = {
                            Heading2(
                                text = "PhotoNest",
                                fontColor = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        actions = {
                            //Button to switch the theme
                            IconButton(onClick = onThemeToggle) {
                                Icon(
                                    imageVector = if (isDarkTheme) {
                                        Icons.Default.LightMode
                                    } else {
                                        Icons.Default.DarkMode
                                    },
                                    contentDescription = "Toggle theme"
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    Box (
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        BottomAppBar(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val items = listOf(
                                BottomNavItem.Home,
                                BottomNavItem.Explore,
                                BottomNavItem.Bookmarks,
                                BottomNavItem.Profile
                            )
                            items.forEach { item ->
                                val selected = currentRoute == item.route

                                NavigationBarItem(
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
                                            imageVector = if (selected) item.iconSelected else item.iconNotSelected,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        NormalText(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                            }
                        }

                    }

                }
            },
            floatingActionButton = {
                if (showBottomBar) {
                    FloatingActionButton(
                        onClick = { navController.navigate(AppDestinations.ADD_POST_ROUTE) },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Post",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            content(paddingValues)
        }}
    }

