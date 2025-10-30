package com.example.photonest.ui.screens.otp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.photonest.R
import com.example.photonest.ui.components.AnnotatedText
import com.example.photonest.ui.components.BackCircleButton
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.NormalText

@Composable
fun OtpVerificationScreen(
    email: String,
    password: String,
    name: String,
    username: String,
    onVerificationSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: OtpViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(email, password, name, username)
    }

    LaunchedEffect(uiState.accountCreated) {
        if (uiState.accountCreated) {
            onVerificationSuccess()
        }
    }

    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }

    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester1.requestFocus()
    }

    LazyColumn (
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                BackCircleButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Image(
                painter = painterResource(R.drawable.email_sent_icon),
                contentDescription = "Email sent icon",
                modifier = Modifier.size(100.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Heading2(
                text = "Verification Code",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "We have sent you a verification code on\n$email",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OtpTextField(
                    value = otp1,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp1 = it
                            if (it.isNotEmpty()) focusRequester2.requestFocus()
                        }
                    },
                    focusRequester = focusRequester1
                )

                OtpTextField(
                    value = otp2,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp2 = it
                            if (it.isNotEmpty()) focusRequester3.requestFocus()
                            else if (it.isEmpty()) focusRequester1.requestFocus()
                        }
                    },
                    focusRequester = focusRequester2
                )

                OtpTextField(
                    value = otp3,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp3 = it
                            if (it.isNotEmpty()) focusRequester4.requestFocus()
                            else if (it.isEmpty()) focusRequester2.requestFocus()
                        }
                    },
                    focusRequester = focusRequester3
                )

                OtpTextField(
                    value = otp4,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp4 = it
                            if (it.isEmpty()) focusRequester3.requestFocus()
                        }
                    },
                    focusRequester = focusRequester4
                )
            }
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }


        item {
            Button(
                onClick = {
                    val fullOtp = otp1 + otp2 + otp3 + otp4
                    if (fullOtp.length == 4) {
                        viewModel.verifyOtp(fullOtp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = otp1.isNotEmpty() && otp2.isNotEmpty() &&
                        otp3.isNotEmpty() && otp4.isNotEmpty() &&
                        !uiState.isVerifyingOtp && !uiState.isCreatingAccount
            ) {
                if (uiState.isVerifyingOtp || uiState.isCreatingAccount) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    NormalText(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }


        item {
            AnnotatedText(
                text1 = "Didn't receive OTP? ",
                text2 = if (uiState.isResendEnabled) "Resend OTP" else "Resend in ${uiState.resendCountdown}s",
                txt2Color = if (uiState.isResendEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                onClickTxt2 = {
                    if (uiState.isResendEnabled){
                        otp1 = ""
                        otp2 = ""
                        otp3 = ""
                        otp4 = ""
                        viewModel.resendOtp()
                    }
                }
            )
        }

        item {
            if (uiState.sendOtpError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.sendOtpError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (uiState.verifyOtpError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.verifyOtpError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (uiState.accountCreationError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.accountCreationError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Success message for OTP sent
            if (uiState.otpSent && uiState.sendOtpError == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "✓ OTP sent successfully!",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Loading state for OTP sending
            if (uiState.isSendingOtp) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sending OTP...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun OtpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .size(64.dp)
            .focusRequester(focusRequester)
            .border(
                width = 2.dp,
                color = if (value.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.6f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
