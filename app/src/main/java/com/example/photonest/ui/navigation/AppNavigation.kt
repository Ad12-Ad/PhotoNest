package com.example.photonest.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.photonest.ui.screens.OtpScreen
import com.example.photonest.ui.screens.home.HomeScreen
import com.example.photonest.ui.screens.MainScaffold
import com.example.photonest.ui.screens.addpost.AddPostBottomSheet
import com.example.photonest.ui.screens.signin.SignInScreen
import com.example.photonest.ui.screens.signup.SignUpScreen
import com.example.photonest.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.SPLASH_ROUTE
) {
    //for the theme switching functionality
    var isDarkTheme by remember { mutableStateOf(false) }

    var showAddPostSheet by remember { mutableStateOf(false) }


    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    //navigation routes which do not have a scaffolding
    val nonScaffoldRoutes = remember {
        setOf(
            AppDestinations.SPLASH_ROUTE,
            AppDestinations.SIGN_IN_ROUTE,
            AppDestinations.SIGN_UP_ROUTE,
            AppDestinations.OTP_ROUTE,
        )
    }

    val shouldShowScaffold = remember(currentRoute) {
        currentRoute !in nonScaffoldRoutes
    }

    if (shouldShowScaffold) {
        MainScaffold(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme },
            navController = navController,
            onAddPostClick = { showAddPostSheet = true}
        ) { paddingValues ->
            NavigationGraph(
                navController = navController,
                startDestination = startDestination,
                paddingValues = paddingValues
            )

            if (showAddPostSheet) {
                AddPostBottomSheet(
                    onDismiss = { showAddPostSheet = false }
                )
            }
        }
    } else {
        NavigationGraph(
            navController = navController,
            startDestination = startDestination,
            paddingValues = PaddingValues(0.dp)
        )
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
                }
            )
        }

        // Authentication Screens
        composable(AppDestinations.SIGN_UP_ROUTE) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(AppDestinations.HOME_ROUTE) },
                onBackClick = { navController.popBackStack() },
                onSignInTxtClick = { navController.navigate(AppDestinations.SIGN_IN_ROUTE) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }

        composable(AppDestinations.SIGN_IN_ROUTE) {
            SignInScreen(
                onSignInSuccess = { navController.navigate(AppDestinations.HOME_ROUTE) },
                onBackClick = { navController.popBackStack() },
                onSignUpTxtClick = { navController.navigate(AppDestinations.SIGN_UP_ROUTE) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
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

        //Home Screen
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }
    }
}