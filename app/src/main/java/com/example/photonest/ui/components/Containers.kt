package com.example.photonest.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.photonest.ui.animations.pressClick
import com.example.photonest.ui.theme.PhotoNestTheme
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.photonest.R

@Composable
fun ElevatedUploadContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showBorder: Boolean = true,
    showDottedBorder: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clip(shape)
            .pressClick(
                enabled = showBorder,
                pressScale = 0.95f,
                onClick = onClick
            ),
        shape = shape,
//        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (showBorder) {
            BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.inverseSurface.copy(0.2f))
                )
            )
        } else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f)
        )
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showDottedBorder){
                        Modifier
                            .padding(8.dp)
                            .dashedBorder(
                                color = MaterialTheme.colorScheme.primary,
                                cornerRadius = 20.dp,
                                dashWidth = 8.dp,
                                dashGap = 6.dp
                            )
                    } else{
                        Modifier
                    }
                )
                .padding(contentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun OuterShadowContainer(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    innerShadowColors: List<Color> = listOf(Color.Black,MaterialTheme.colorScheme.primaryContainer.copy(0.5f)),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .innerShadow(
                shape = shape,
                color = innerShadowColors.get(0),
                offsetX = (-2).dp,
                offsetY = (-2).dp
            )
            .innerShadow(
                shape = shape,
                color = innerShadowColors.get(1),
                offsetX = 2.dp,
                offsetY = 2.dp
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    dashWidth: Dp,
    dashGap: Dp
) = this.drawBehind {
    val stroke = Stroke(
        width = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashWidth.toPx(), dashGap.toPx())
        )
    )

    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(
            cornerRadius.toPx(),
            cornerRadius.toPx()
        )
    )
}

internal fun Modifier.innerShadow(
    shape: Shape,
    color: Color,
    blur: Dp = 4.dp,
    offsetX: Dp = 2.dp,
    offsetY: Dp = 2.dp,
    spread: Dp = 0.dp
) = drawWithContent {

    drawContent()

    drawIntoCanvas { canvas ->
        val shadowSize = Size(
            size.width + spread.toPx(),
            size.height + spread.toPx()
        )

        val outline = shape.createOutline(
            shadowSize,
            layoutDirection,
            this
        )

        val paint = Paint().apply {
            this.color = color
        }

        canvas.saveLayer(size.toRect(), paint)
        canvas.drawOutline(outline, paint)

        paint.asFrameworkPaint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (blur.toPx() > 0) {
                maskFilter = BlurMaskFilter(
                    blur.toPx(),
                    BlurMaskFilter.Blur.NORMAL
                )
            }
        }

        canvas.translate(offsetX.toPx(), offsetY.toPx())
        canvas.drawOutline(outline, paint)
        canvas.restore()
    }
}

@Preview(showBackground = true)
@Composable
fun ElevatedUploadContainerPreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ElevatedUploadContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    onClick = {}
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tap to upload image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Without Border",
    showBackground = true
)
@Composable
fun ElevatedUploadContainerNoBorderPreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ElevatedUploadContainer(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.5f))
                        .fillMaxWidth()
                        .height(150.dp),
                    showBorder = false,
                    showDottedBorder = false,
                    onClick = {}
                ) {
                    Text(
                        text = "No Border Container",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(
    name = "With Content Padding",
    showBackground = true
)
@Composable
fun ElevatedUploadContainerWithPaddingPreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                ElevatedUploadContainer(
                    modifier = Modifier.size(200.dp),
                    contentPadding = PaddingValues(24.dp),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Square Container",
    showBackground = true
)
@Composable
fun ElevatedUploadContainerSquarePreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                ElevatedUploadContainer(
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(180.dp),
                    elevation = 4.dp,
                    onClick = {}
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OuterShadowContainer(
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                painter = painterResource( R.drawable.add_image_icon),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    name = "Multiple Containers - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Multiple Containers - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MultipleOuterShadowContainersPreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OuterShadowContainer(
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(12.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(40.dp)
                    )
                }

                OuterShadowContainer(
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(12.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_apple),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(40.dp)
                    )
                }

                OuterShadowContainer(
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(12.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logo_phone),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "All Containers",
    showBackground = true,
    heightDp = 800
)
@Composable
fun AllContainersPreview() {
    PhotoNestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "ElevatedUploadContainer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                ElevatedUploadContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    onClick = {}
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Upload Photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OuterShadowContainer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OuterShadowContainer(
                        modifier = Modifier.size(100.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon_camera),
                            contentDescription = "Google Logo"
                        )
                    }

                    OuterShadowContainer(
                        modifier = Modifier.size(100.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
