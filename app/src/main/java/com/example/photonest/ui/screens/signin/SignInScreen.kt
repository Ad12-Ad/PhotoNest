package com.example.photonest.ui.screens.signin

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.R
import com.example.photonest.ui.components.AnnotatedText
import com.example.photonest.ui.components.BackTxtBtn
import com.example.photonest.ui.components.Heading1
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.MyAlertDialog
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.OnBoardingTextField
import com.example.photonest.ui.components.OnboardingCircleBtn
import com.example.photonest.ui.components.ShowHidePasswordTextField
import com.example.photonest.ui.components.SignSocialButtons
import com.example.photonest.ui.theme.bodyFontFamily

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(),
    onSignInSuccess: (String) -> Unit,
    onSignUpTxtClick: () -> Unit,
    onBackClick: () -> Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(uiState.isSignInSuccessful) {
        if (uiState.isSignInSuccessful) {
            onSignInSuccess(uiState.email)
            viewModel.resetSignInSuccess()
        }
    }

    SignInContent(
        uiState = uiState,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onSignInTxtClick = onSignUpTxtClick,
        onSignInClick = viewModel::signIn,
        onBackClick = onBackClick,
        onClearEmail = {viewModel.updateEmail("")},
        modifier = modifier
    )

    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = viewModel::dismissErrorDialog,
        title = "Sign In Failed",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = viewModel::dismissErrorDialog
    )
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    uiState: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit = {},
    onSignInTxtClick: () -> Unit,
    onBackClick: () -> Boolean,
    onClearEmail: () -> Unit,
    onSignInSuccess: () -> Unit = {},
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
                BackTxtBtn(onClick = { onBackClick() }, modifier = Modifier.padding(top = 12.dp))
            }
        }
        item {
            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                OnBoardingTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    isError = uiState.emailError != null,
                    errorMessage = {
                        Text(
                            text = uiState.emailError?: "",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = bodyFontFamily,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    },
                    prefix = {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_profile_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClearSearch = onClearEmail
                )
                ShowHidePasswordTextField(
                    label = "Password",
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    isError = uiState.passwordError != null,
                    errorMessage = {
                        Text(
                            text = uiState.passwordError?: "",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = bodyFontFamily,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    },
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    AnnotatedText(
                        text1 = "Don't have an account.", text2 = "Sign Up", onClickTxt2 = {onSignInTxtClick()},
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
                    onClick = { onSignInClick() },
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

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun SignInPrev() {
//    PhotoNestTheme {
//        SignInContent(
//            onEmailChange = {},
//            onPasswordChange = {},
//            onBackClick = {false},
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        )
//    }
//}