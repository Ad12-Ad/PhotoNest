package com.example.photonest.ui.screens.signup

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
fun SignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel(),
    onSignUpSuccess: (String, String, String, String) -> Unit,
    onSignInTxtClick: () -> Unit,
    onBackClick: () -> Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSignUpSuccessful) {
        LaunchedEffect(Unit) {
            onSignUpSuccess(
                uiState.email,
                uiState.password,
                uiState.name,
                uiState.username
            )
            viewModel.resetSignUpSuccess()
        }
    }
    SignUpContent(
        uiState = uiState,
        onEmailChange = viewModel::updateEmail,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onNameChange = viewModel::updateName,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onSignUpClick = { viewModel.signUp() },
        onSignInTxtClick = { onSignInTxtClick() },
        onBackClick = onBackClick,
        onSignUpSuccess = onSignUpSuccess,
        modifier = modifier,
        onClearName = {viewModel.updateName("")},
        onClearUserName = {viewModel.updateUsername("")},
        onClearEmail = {viewModel.updateEmail("")}
    )
    MyAlertDialog(
        shouldShowDialog = uiState.showErrorDialog,
        onDismissRequest = viewModel::dismissErrorDialog,
        title = "Sign Up Failed",
        text = uiState.error ?: "An unknown error occurred",
        confirmButtonText = "OK",
        onConfirmClick = viewModel::dismissErrorDialog
    )
}

@Composable
fun SignUpContent(
    uiState: SignUpUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onSignInTxtClick: () -> Unit,
    onBackClick: () -> Boolean,
    onClearName: () -> Unit,
    onClearUserName: () -> Unit,
    onClearEmail: () -> Unit,
    onSignUpSuccess: (String, String, String, String) -> Unit,
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
                BackTxtBtn(onClick = { onBackClick() }, modifier = Modifier.padding(top = 12.dp))
            }
        }
        item {
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
        }
        item {
            OnBoardingTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = "User Name",
                isError = uiState.usernameError != null,
                errorMessage = {
                    Text(
                        text = uiState.usernameError?: "",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = bodyFontFamily,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                },
                onClearSearch = onClearUserName
            )
        }

        item {
            OnBoardingTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = "Name",
                isError = uiState.nameError != null,
                errorMessage = {
                    Text(
                        text = uiState.nameError?: "",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = bodyFontFamily,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                },
                onClearSearch = onClearName
            )
        }

        item {
            ShowHidePasswordTextField(
                label = "Password",
                value = uiState.password,
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
                onValueChange = onPasswordChange,
            )
        }
        item {
            ShowHidePasswordTextField(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                isError = uiState.confirmPasswordError != null,
                errorMessage = {
                    Text(
                        text = uiState.confirmPasswordError?: "",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = bodyFontFamily,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                },
                onValueChange = onConfirmPasswordChange
            )
        }
        item {
            AnnotatedText(
                text1 = "If already have an account.", text2 = "Sign In", onClickTxt2 = {onSignInTxtClick()},
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
                    onClick = { onSignUpClick() },
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

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun SignUpPrev() {
//    PhotoNestTheme {
//        val uiState by viewModel.uiState.collectAsState()
//
//        SignUpContent(
//            uiState = uiState,
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}