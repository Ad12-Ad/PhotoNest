package com.example.photonest.ui.screens.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Boolean,
) {
    SignInContent(
        onEmailChange = {},
        onPasswordChange = {},
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit = {},
    onBackClick: () -> Boolean,
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
                    value = "",
                    onValueChange = onEmailChange,
                    label = "Email",
                    isError = false,
                    errorMessage = {},
                    prefix = {
                        Icon(
                            painter = painterResource(id = R.drawable.user_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                ShowHidePasswordTextField(
                    label = "Password",
                    value = "",
                    onValueChange = onPasswordChange,
                    isError = false,
                    errorMessage = { },
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    AnnotatedText(
                        text1 = "Don't have an account.", text2 = "Sign Up",
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
                    enabled = true
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInPrev() {
    PhotoNestTheme {
        SignInContent(
            onEmailChange = {},
            onPasswordChange = {},
            onBackClick = {false},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}