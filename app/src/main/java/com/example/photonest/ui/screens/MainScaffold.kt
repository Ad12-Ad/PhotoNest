package com.example.photonest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.photonest.R
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
    onAddPostClick: () -> Unit,
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
                                    .size(152.dp)
                            )
                        },
                        actions = {
                            FilledIconButton(
                                onClick = onThemeToggle,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                                )
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) {
                                        Icons.Default.LightMode
                                    } else {
                                        Icons.Default.DarkMode
                                    },
                                    contentDescription = "Toggle theme",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth(),
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

                            if (item == BottomNavItem.AddPost){
                                FloatingActionButton(
                                    modifier = Modifier.padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                                    onClick = { onAddPostClick() },
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ) {
                                    Image(
                                        painter = painterResource(id = item.iconSelected),
                                        contentDescription = "Add Post",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }else{
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
                                            painter = painterResource(id = if (selected) item.iconSelected else item.iconNotSelected),
                                            contentDescription = item.title,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        NormalText(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                )
                            }

                        }
                    }
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }}
    }

