package com.example.photonest.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.R
import com.example.photonest.ui.components.ButtonOnboarding
import com.example.photonest.ui.components.Heading1
import com.example.photonest.ui.components.NormalText
import kotlinx.coroutines.delay

private const val ANIMATION_DURATION = 1000
private const val ANIMATION_DELAY = 2000

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    var animationState by rememberSaveable { mutableStateOf(AnimationState.LogoZoom) }

    LaunchedEffect(loginState, animationState) {
//        delay(ANIMATION_DELAY.toLong())
        when (animationState) {
            AnimationState.LogoZoom -> {
                animationState = AnimationState.LogoFadeOut
            }
            AnimationState.LogoFadeOut -> {
                delay(ANIMATION_DELAY.toLong())
                if (loginState is LoginState.LoggedIn) {
                    onNavigateToHome()
                } else {
                    animationState = AnimationState.BackgroundTransition
                }
            }
            AnimationState.BackgroundTransition ->{
                delay(ANIMATION_DELAY.toLong())
                animationState = AnimationState.ContentFadeIn
            }
            AnimationState.ContentFadeIn -> {
                // Wait for user interaction
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)) {

        val backgroundHeightAnimation by animateFloatAsState(
            targetValue = if (animationState == AnimationState.BackgroundTransition || animationState == AnimationState.ContentFadeIn) 0.5f else 1f,
            animationSpec = tween(ANIMATION_DURATION)
        )

        Card (
            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(backgroundHeightAnimation),
            elevation = CardDefaults.cardElevation(12.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.bg_photo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        val logoScale by animateFloatAsState(
            targetValue = if (animationState == AnimationState.LogoZoom) 1f else 1.5f,
            animationSpec = tween(ANIMATION_DURATION)
        )
        val logoAlpha by animateFloatAsState(
            targetValue = if (animationState == AnimationState.LogoFadeOut) 1f else 0f,
            animationSpec = tween(ANIMATION_DURATION)
        )

        if (animationState == AnimationState.LogoZoom || animationState == AnimationState.LogoFadeOut) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "PhotoNest Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = animationState == AnimationState.ContentFadeIn && loginState is LoginState.NotLoggedIn,
            enter = fadeIn(animationSpec = tween(ANIMATION_DURATION))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Heading1(
                        text = "Dedicated to all\nPhotographers",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 44.sp,
                        lineHeight = 68.sp,
                        fontColor = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NormalText(
                        text = "The Right Place\nTo Publish Your Best\nPhotographies...",
                        fontSize = 24.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Thin,
                        fontColor = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ButtonOnboarding(
                        buttonText = "Create Account",
                        postfixIcon = {
                            Icon(imageVector = Icons.Filled.ArrowForwardIos, contentDescription = "Forward arrow")
                        },
                        textSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(60.dp)
                            .fillMaxWidth(),
                        onClick = onNavigateToSignUp
                    )
                    Spacer(modifier = Modifier.height(45.dp))
                    ButtonOnboarding(
                        buttonText = "Log In",
                        textSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(60.dp)
                            .fillMaxWidth(),
                        onClick = onNavigateToSignIn,
                        buttonColors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.surfaceContainer
                        ),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class AnimationState {
    LogoZoom,
    LogoFadeOut,
    BackgroundTransition,
    ContentFadeIn
}