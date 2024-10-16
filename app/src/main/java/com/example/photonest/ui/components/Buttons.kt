package com.example.photonest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
        enabled = true,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Unspecified
        ),
        content = {
            Image(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
    )
}

@Composable
fun BackTxtBtn(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp, minWidth = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, kelly_green, RoundedCornerShape(16.dp))
            .clickable { onClick },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Back",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                fontWeight = FontWeight.Medium,
                lineHeight = 25.sp,
                color = kelly_green
            )
        )
    }
}

@Composable
fun OnboardingCircleBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = kelly_green,
        disabledContainerColor = disabled_kelly_green
    ),
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = iconColors,
        content = {
            Image(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer(rotationZ = 180f)
                    .size(18.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
            )
        }
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Prev() {
    Column {
        OnboardingCircleBtn(onClick = { /*TODO*/ },)
        BackButton {

        }
    }
}