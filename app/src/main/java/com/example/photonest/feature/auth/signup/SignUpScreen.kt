package com.example.photonest.feature.auth.signup

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.photonest.feature.auth.signup.model.SignUpEffect
import com.example.photonest.feature.auth.signup.model.SignUpEvent
import com.example.photonest.feature.auth.signup.model.SignUpUiState
import com.example.photonest.app.theme.PhotoNestTheme
import com.example.photonest.app.theme.bodyFontFamily
import com.example.photonest.core.utils.ObserveAsEvents
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignUpScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(flow = viewModel.effect) { effects ->
        when(effects){
            SignUpEffect.NavigateToOtp -> {
                navController.navigate("otp/${uiState.email}/${uiState.password}") {
                    popUpTo(AppDestinations.SIGN_UP_ROUTE) { inclusive = false }
                }
            }
            is SignUpEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = effects.message
                    )
                }
            }

            SignUpEffect.NavigateBack -> {
                navController.popBackStack()
            }

            SignUpEffect.NavigateSignIn -> {
                navController.navigate(AppDestinations.SIGN_IN_ROUTE) {
                    popUpTo(AppDestinations.SIGN_UP_ROUTE) { inclusive = true }
                }
            }
        }
    }

    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ){
        SignUpContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = modifier
        )
    }
}

@Composable
fun SignUpContent(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Row (
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Heading1(text = "Create an \naccount", fontColor = MaterialTheme.colorScheme.primary)
                BackTxtBtn(onClick = {onEvent(SignUpEvent.BackClicked)}, modifier = Modifier.padding(top = 12.dp))
            }
        }
        item {
            OnBoardingTextField(
                value = uiState.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = "Email",
                isError = uiState.emailError != null,
                errorMessage = { ErrorTxt(error = uiState.emailError) },
                prefix = { PrefixIcon(icon = R.drawable.icon_profile_outlined) },
                onClearSearch = { onEvent(SignUpEvent.EmailChanged("")) }
            )
        }

        item {
            ShowHidePasswordTextField(
                label = "Password",
                value = uiState.password,
                isError = uiState.passwordError != null,
                errorMessage = { ErrorTxt(uiState.passwordError) },
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            )
        }
        item {
            ShowHidePasswordTextField(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                isError = uiState.confirmPasswordError != null,
                errorMessage = { ErrorTxt(uiState.confirmPasswordError) },
                onValueChange = {onEvent(SignUpEvent.ConfirmPasswordChanged(it)) }
            )
        }
        item {
            AnnotatedText(
                text1 = "If already have an account.", text2 = "Sign In",
                onClickTxt2 = {onEvent(SignUpEvent.SignInTxtClicked)},
                modifier = Modifier.height(24.dp)
            )
        }
        item {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Heading2(text = "Register", fontColor = MaterialTheme.colorScheme.onBackground)
                OnboardingCircleBtn(
                    onClick = { onEvent(SignUpEvent.Submit) },
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
                NormalText(text = "Sign up with")
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

@Composable
fun ErrorTxt(error: String?, modifier: Modifier = Modifier) {
    error?.let {
        Text(
            text = it,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = bodyFontFamily,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            ),
            modifier = modifier
        )
    }
}


@Composable
fun PrefixIcon(icon: Int) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier.size(20.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpPrev() {
    PhotoNestTheme {
        SignUpContent(
            uiState = SignUpUiState(
                email = "ashok34r98@gmail.com",
            ),
            onEvent = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}