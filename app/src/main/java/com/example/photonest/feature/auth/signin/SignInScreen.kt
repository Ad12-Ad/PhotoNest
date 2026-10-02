package com.example.photonest.feature.auth.signin

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.photonest.R
import com.example.photonest.core.ui.components.AnnotatedText
import com.example.photonest.core.ui.components.BackTxtBtn
import com.example.photonest.core.ui.components.Heading1
import com.example.photonest.core.ui.components.Heading2
import com.example.photonest.core.ui.components.NormalText
import com.example.photonest.core.ui.components.OnBoardingTextField
import com.example.photonest.core.ui.components.OnboardingCircleBtn
import com.example.photonest.core.ui.components.ShowHidePasswordTextField
import com.example.photonest.core.ui.components.SignSocialButtons
import com.example.photonest.app.navigation.AppDestinations
import com.example.photonest.feature.auth.signin.model.SignInEffects
import com.example.photonest.feature.auth.signin.model.SignInEvents
import com.example.photonest.feature.auth.signin.model.SignInState
import com.example.photonest.feature.auth.signup.ErrorTxt
import com.example.photonest.feature.auth.signup.PrefixIcon
import com.example.photonest.core.utils.ObserveAsEvents
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(flow = viewModel.effects) { effects ->
        when(effects){
            SignInEffects.NavigateBack -> {
                navController.popBackStack()
            }
            SignInEffects.NavigateToHome -> {
                navController.navigate(AppDestinations.HOME_ROUTE) {
                    popUpTo(AppDestinations.SIGN_IN_ROUTE) { inclusive = true }
                }
            }
            SignInEffects.NavigateToSignUp -> {
                navController.navigate(AppDestinations.SIGN_UP_ROUTE) {
                    popUpTo(AppDestinations.SIGN_IN_ROUTE) { inclusive = true }
                }
            }
            is SignInEffects.ShowSnackBar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = effects.message
                    )
                }
            }
        }
    }

    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ){
        SignInContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = modifier
        )
    }
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    uiState: SignInState,
    onEvent: (SignInEvents) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(40.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Row (
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Heading1(text = "Let's sign in", fontColor = MaterialTheme.colorScheme.primary)
                BackTxtBtn(onClick = { onEvent(SignInEvents.BackClick) }, modifier = Modifier.padding(top = 12.dp))
            }
        }
        item {
            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                OnBoardingTextField(
                    value = uiState.email,
                    onValueChange = {onEvent(SignInEvents.EmailChanged(it))},
                    label = "Email",
                    isError = uiState.emailError != null,
                    errorMessage = { ErrorTxt(error = uiState.emailError) },
                    prefix = { PrefixIcon(icon = R.drawable.icon_profile_outlined) },
                    onClearSearch = { onEvent(SignInEvents.EmailChanged("")) }
                )
                ShowHidePasswordTextField(
                    label = "Password",
                    value = uiState.password,
                    onValueChange = { onEvent(SignInEvents.PasswordChanged(it)) },
                    isError = uiState.passwordError != null,
                    errorMessage = { ErrorTxt(uiState.passwordError) },
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    AnnotatedText(
                        text1 = "Don't have an account.", text2 = "Sign Up", onClickTxt2 = {onEvent(
                            SignInEvents.SignUpTxtClick)},
                        modifier = Modifier.height(24.dp)
                    )
                    AnnotatedText(
                        text1 = "", text2 = "Forgot Password?",
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        item {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Heading2(text = "Continue", fontColor = MaterialTheme.colorScheme.onBackground)
                OnboardingCircleBtn(
                    onClick = { onEvent(SignInEvents.SignInClick)},
                    enabled = uiState.isInputValid && !uiState.isLoading
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NormalText(text = "Sign In With")
                Spacer(modifier = Modifier.height(10.dp))
                SignSocialButtons()
            }
        }
    }
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
