package com.example.photonest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.disabled_kelly_green
import com.example.compose.kelly_green
import com.example.compose.white
import com.example.photonest.R

@Composable
fun ButtonOnboarding(
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = white,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = kelly_green,
        disabledContainerColor = disabled_kelly_green
    ),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(2.dp),
    prefixIcon: @Composable () -> Unit? = { null }
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        colors = buttonColors,
        shape = RoundedCornerShape(12.dp),
        elevation = elevation
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            prefixIcon()?.let {
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = buttonText,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Unspecified
        ),
        content = {
            Image(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
    )
}