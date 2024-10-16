package com.example.photonest.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.photonest.R

@Composable
fun SignSocialButtons(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SocialButton(
            image = R.drawable.logo_google,
            modifier = Modifier.size(50.dp)
        )
        SocialButton(
            image = R.drawable.logo_apple,
            modifier = Modifier.size(50.dp)
        )
        SocialButton(
            image = R.drawable.logo_phone,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun SocialButton(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
    ),
    elevation: CardElevation = CardDefaults.cardElevation(10.dp),
    shape: Shape = CircleShape,
    onClick: () -> Unit = {}
) {
    Card(
        colors = colors,
        elevation = elevation,
        modifier = modifier.then(
            Modifier
                .clip(shape)
                .clickable { onClick() })
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        )
    }
}
