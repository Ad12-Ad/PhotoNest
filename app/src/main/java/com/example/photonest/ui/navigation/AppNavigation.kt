package com.example.photonest.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.photonest.ui.screens.OtpScreen
import com.example.photonest.ui.screens.home.HomeScreen
import com.example.photonest.ui.screens.MainScaffold
import com.example.photonest.ui.screens.addpost.AddPostScreen
import com.example.photonest.ui.screens.bookmarks.BookmarksScreen
import com.example.photonest.ui.screens.explore.ExploreScreen
import com.example.photonest.ui.screens.profile.ProfileScreen
import com.example.photonest.ui.screens.signin.SignInScreen
import com.example.photonest.ui.screens.signup.SignUpScreen
import com.example.photonest.ui.screens.splash.SplashScreen
import com.example.photonest.ui.screens.notification.NotificationScreen
import com.example.photonest.ui.screens.postdetail.PostDetailScreen
import com.example.photonest.ui.screens.profile.EditProfileScreen
import com.example.photonest.ui.screens.profile.LikedPostsScreen
import com.example.photonest.ui.screens.profile.YourPostsScreen
import com.example.photonest.ui.screens.profile.userprofile.UserProfileScreen
import com.example.photonest.ui.screens.profile.userprofile.UserProfileViewModel
import com.example.photonest.ui.screens.settings.SettingsScreen
import com.example.photonest.ui.theme.PhotoNestTheme

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.SPLASH_ROUTE
) {
    val systemTheme = isSystemInDarkTheme()
    // For the theme switching functionality
    var isDarkTheme by rememberSaveable { mutableStateOf(systemTheme) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Navigation routes which do not have scaffolding
    val nonScaffoldRoutes = remember {
        setOf(
            AppDestinations.SPLASH_ROUTE,
            AppDestinations.SIGN_IN_ROUTE,
            AppDestinations.SIGN_UP_ROUTE,
            AppDestinations.OTP_ROUTE,
            AppDestinations.EDIT_PROFILE_ROUTE,
            AppDestinations.ADD_POST_ROUTE,
            "post_detail/{postId}",
            AppDestinations.POST_DETAIL_ROUTE,
            AppDestinations.NOTIFICATIONS_ROUTE,
            AppDestinations.SETTINGS_ROUTE,
            AppDestinations.LIKED_POSTS_ROUTE,
            AppDestinations.YOUR_POSTS_ROUTE,
            "${AppDestinations.USER_PROFILE_ROUTE}/{userId}"
        )
    }

    val shouldShowScaffold = remember(currentRoute) {
        currentRoute !in nonScaffoldRoutes
    }

    PhotoNestTheme(darkTheme = isDarkTheme) {
        if (shouldShowScaffold) {
            MainScaffold(
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme },
                navController = navController,
                onAddPostClick = {
                    navController.navigate(AppDestinations.ADD_POST_ROUTE)
                },
                onNotificationClick = {
                    navController.navigate(AppDestinations.NOTIFICATIONS_ROUTE)
                }
            ) { paddingValues ->
                NavigationGraph(
                    navController = navController,
                    startDestination = startDestination,
                    paddingValues = paddingValues
                )
            }
        } else {
            NavigationGraph(
                navController = navController,
                startDestination = startDestination,
                paddingValues = PaddingValues(0.dp)
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Splash Screen
        composable(AppDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(AppDestinations.SIGN_IN_ROUTE)
                },
                onNavigateToSignUp = {
                    navController.navigate(AppDestinations.SIGN_UP_ROUTE)
                },
                viewModel = hiltViewModel()
            )
        }

        // Authentication Screens
        composable(AppDestinations.SIGN_UP_ROUTE) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.SIGN_UP_ROUTE) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onSignInTxtClick = {
                    navController.navigate(AppDestinations.SIGN_IN_ROUTE) {
                        popUpTo(AppDestinations.SIGN_UP_ROUTE) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding(),
                viewModel = hiltViewModel()
            )
        }

        composable(AppDestinations.SIGN_IN_ROUTE) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.SIGN_IN_ROUTE) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onSignUpTxtClick = {
                    navController.navigate(AppDestinations.SIGN_UP_ROUTE) {
                        popUpTo(AppDestinations.SIGN_IN_ROUTE) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding(),
                viewModel = hiltViewModel()
            )
        }

        composable(AppDestinations.OTP_ROUTE) {
            OtpScreen(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }

        // Main App Screens (with bottom navigation)
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onUserClick = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                }
            )
        }

        composable(AppDestinations.EXPLORE_ROUTE) {
            ExploreScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                viewModel = hiltViewModel()
            )
        }

        composable(AppDestinations.BOOKMARKS_ROUTE) {
            BookmarksScreen(
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId.toString()))
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                viewModel = hiltViewModel(),
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }

        // Add this inside the NavigationGraph NavHost
        composable(AppDestinations.ADD_POST_ROUTE) {
            AddPostScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.ADD_POST_ROUTE) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                viewModel = hiltViewModel()
            )
        }


        composable(AppDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateToSettingScreen = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onNavToPersonalDetails = { navController.navigate(AppDestinations.EDIT_PROFILE_ROUTE) },
                onNavToBookmarkCollection = { navController.navigate(AppDestinations.BOOKMARKS_ROUTE) },
                onNavToLikedPosts = { navController.navigate(AppDestinations.LIKED_POSTS_ROUTE) },
                onNavToYourPosts = { navController.navigate(AppDestinations.YOUR_POSTS_ROUTE) },
                onNavToNotifications = { navController.navigate(AppDestinations.NOTIFICATION_ROUTE) },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                },
                onNavToTheme = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onLogOut = {
                    navController.navigate(AppDestinations.SIGN_IN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                viewModel = hiltViewModel()
            )
        }

        // Post Detail Screen
        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }

        composable(AppDestinations.NOTIFICATIONS_ROUTE) {
            NotificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                },
                onNavigateToPost = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }

        composable(AppDestinations.LIKED_POSTS_ROUTE) {
            LikedPostsScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }
        composable(AppDestinations.YOUR_POSTS_ROUTE) {
            YourPostsScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }


        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


// Edit Profile Screen
        composable(AppDestinations.EDIT_PROFILE_ROUTE) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            )
        }

        composable(
            route = "${AppDestinations.USER_PROFILE_ROUTE}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

            UserProfileScreen(
                userId = userId,
                onBackClick = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(AppDestinations.getUserProfileRoute(userId))
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                viewModel = hiltViewModel<UserProfileViewModel>()
            )
        }
    }
}