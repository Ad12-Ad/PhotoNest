package com.example.photonest.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photonest.ui.screens.OtpScreen
import com.example.photonest.ui.screens.signin.SignInScreen
import com.example.photonest.ui.screens.signup.SignUpScreen
import com.example.photonest.ui.screens.splash.SplashScreen

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val SIGN_UP_ROUTE = "sign_up"
    const val SIGN_IN_ROUTE = "sign_in"
    const val OTP_ROUTE = "otp"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.SPLASH_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onTimeout = {navController.navigate(AppDestinations.SIGN_UP_ROUTE) }
            )
        }
        composable(AppDestinations.SIGN_UP_ROUTE) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(AppDestinations.OTP_ROUTE) },
                onBackClick = { navController.popBackStack() },
                onSignInTxtClick = {navController.navigate(AppDestinations.SIGN_IN_ROUTE) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }
        composable(AppDestinations.SIGN_IN_ROUTE) {
            SignInScreen(
                onSignInSuccess = { navController.navigate(AppDestinations.OTP_ROUTE) },
                onBackClick = { navController.popBackStack() },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }
        composable(AppDestinations.OTP_ROUTE){
            OtpScreen(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .safeContentPadding()
            )
        }
    }
}
