package com.example.photonest.ui.screens.singup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
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
import com.example.photonest.ui.components.SocialButton
import com.example.photonest.ui.components.annotatedText

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    SignInContent(
//        uiState = uiState,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun SignInContent(
//    uiState: SignUpUiState,
//    onEmailChange: (String) -> Unit,
//    onPasswordChange: (String) -> Unit,
//    onSignInClick: () -> Unit,
//    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row (
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            Heading1(text = "Create an \naccount")
            BackTxtBtn(modifier = Modifier.padding(top = 12.dp)) {}
        }
        OnBoardingTextField(
            value = "",
            onValueChange = {},
            label = "Username or Email",
            focusRequester = FocusRequester(),
            prefix = {
                Icon(
                    painter = painterResource(id = R.drawable.user_logo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        OnBoardingTextField(
            value = "",
            onValueChange = {},
            label = "Name",
            focusRequester = FocusRequester()
        )
        ShowHidePasswordTextField(label = "Password")
        ShowHidePasswordTextField(label = "Confirm Password")
        AnnotatedText(
            text1 = "If already have an account", text2 = "Sign In",
            modifier = Modifier.height(24.dp)
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Heading2(text = "Register", fontColor = Color.Black)
            OnboardingCircleBtn(onClick = {  })
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NormalText(text = "sign up with")
            Spacer(modifier = Modifier.height(10.dp))
            SignSocialButtons()
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpPrev() {
    SignInContent(modifier = Modifier.padding(16.dp))
}