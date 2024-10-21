package com.example.photonest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.R
import com.example.photonest.ui.theme.bodyFontFamily

@Composable
fun ButtonOnboarding(
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    textSize: TextUnit = 16.sp,
    textWeight: FontWeight = FontWeight.Normal,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.4f)
    ),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(4.dp),
    prefixIcon: @Composable (() -> Unit)? = null,
    postfixIcon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = buttonColors,
        shape = shape,
        elevation = elevation,
        modifier = modifier
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (prefixIcon != null) {
                prefixIcon()
                Spacer(Modifier.width(16.dp))
            }

            Text(
                text = buttonText,
                style = TextStyle(
                    fontSize = textSize,
                    fontWeight = textWeight,
                    color = textColor
                )
            )

            if (postfixIcon != null) {
                Spacer(Modifier.width(16.dp))
                postfixIcon()
            }
        }
    }
}


@Composable
fun BackTxtBtn(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp, minWidth = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Back",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = bodyFontFamily,
                fontWeight = FontWeight.Medium,
                lineHeight = 25.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun BackCircleButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier.then(Modifier.size(50.dp)),
        onClick = onClick,
        enabled = true,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        content = {
            Image(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    )
}

@Composable
fun OnboardingCircleBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.4f)
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
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    )
}