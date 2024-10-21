package com.example.photonest.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.photonest.ui.components.Heading2
import com.example.photonest.ui.components.NormalText
import com.example.photonest.ui.theme.PhotoNestTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    HomeScreenContent(modifier = modifier)
}

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Heading2(text = "Home Screen")
        NormalText(text = "Coming Soon")
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun Prev() {
    PhotoNestTheme {
        HomeScreenContent(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .safeContentPadding()
        )
    }
}