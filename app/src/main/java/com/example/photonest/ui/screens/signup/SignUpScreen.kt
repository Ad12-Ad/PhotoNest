package com.example.photonest.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.R
import com.example.photonest.ui.components.AnnotatedText
import com.example.photonest.ui.components.BackTxtBtn
import com.example.photonest.ui.components.Heading1
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.components.OnBoardingTextField
import com.example.photonest.ui.components.OnboardingCircleBtn
import com.example.photonest.ui.components.ShowHidePasswordTextField
import com.example.photonest.ui.components.SignSocialButtons
import com.example.photonest.ui.theme.PhotoNestTheme

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel(),
    onSignInSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SignUpContent(
        uiState = uiState,
        onEmailChange = viewModel::updateEmail,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onSignUpClick = {},
        onSignUpSuccess = onSignInSuccess,
        modifier = modifier
    )
}

@Composable
fun SignUpContent(
    uiState: SignUpUiState,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onSignUpSuccess: () -> Unit,
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
                BackTxtBtn(modifier = Modifier.padding(top = 12.dp)) {}
            }
        }
        item {
            OnBoardingTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Username or Email",
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.user_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        item {
            OnBoardingTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = "Name",
            )
        }

        item {
            ShowHidePasswordTextField(
                label = "Password",
                value = uiState.password,
                onValueChange = onPasswordChange
            )
        }
        item {
            ShowHidePasswordTextField(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange
            )
        }
        item {
            AnnotatedText(
                text1 = "If already have an account.", text2 = "Sign In",
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
                OnboardingCircleBtn(onClick = {  })
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
        CircularProgressIndicator()
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