package com.example.photonest.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.NormalText

@Composable
fun OtpScreen(modifier: Modifier = Modifier) {
    OtpScreenContent(modifier = modifier)
}

@Composable
fun OtpScreenContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Heading2(text = "OTP Screen")
        NormalText(text = "work in progress....")
    }
}