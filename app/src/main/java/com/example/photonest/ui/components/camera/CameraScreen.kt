package com.example.photonest.ui.components.camera

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onImageSelectedFromGallery: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var cameraController by remember { mutableStateOf<LifecycleCameraController?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var recentImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showFlash by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recentImages = loadRecentImages(context)
    }

    DisposableEffect(isFrontCamera) {
        val controller = LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            bindToLifecycle(lifecycleOwner)
            cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        }
        cameraController = controller

        if (isFrontCamera) {
            isTorchEnabled = false
        }

        onDispose {
            controller.unbind()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            TopControls(
                isTorchEnabled = isTorchEnabled,
                isFrontCamera = isFrontCamera,
                onClose = onClose,
                onToggleTorch = {
                    if (!isFrontCamera) {
                        isTorchEnabled = !isTorchEnabled
                        cameraController?.enableTorch(isTorchEnabled)
                    }
                },
                onFlipCamera = {
                    isFrontCamera = !isFrontCamera
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }.also { previewView ->
                            cameraController?.let { previewView.controller = it }
                        }
                    },
                    update = { previewView ->
                        previewView.controller = cameraController
                    }
                )
            }

            BottomControls(
                isCapturing = isCapturing,
                recentImages = recentImages,
                onCapture = {
                    if (!isCapturing && cameraController != null) {
                        isCapturing = true
                        showFlash = true
                        scope.launch {
                            delay(100)
                            showFlash = false
                            captureImage(
                                cameraController = cameraController!!,
                                context = context,
                                onSuccess = { uri ->
                                    isCapturing = false
                                    onImageCaptured(uri)
                                },
                                onError = { error ->
                                    isCapturing = false
                                    Log.e("Camera", "Capture error: $error")
                                }
                            )
                        }
                    }
                },
                onImageSelected = { uri ->
                    onImageSelectedFromGallery(uri)
                }
            )
        }

        // Flash animation overlay
        AnimatedVisibility(
            visible = showFlash,
            enter = fadeIn(animationSpec = tween(50)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun TopControls(
    isTorchEnabled: Boolean,
    isFrontCamera: Boolean,
    onClose: () -> Unit,
    onToggleTorch: () -> Unit,
    onFlipCamera: () -> Unit
) {
    // Slide in from top animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated close button
            AnimatedIconButton(
                onClick = onClose,
                icon = Icons.Default.Close
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Animated torch button
                AnimatedIconButton(
                    onClick = onToggleTorch,
                    enabled = !isFrontCamera,
                    icon = if (isTorchEnabled) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    backgroundColor = if (isTorchEnabled) Color(0xFFFFD700).copy(alpha = 0.6f)
                    else Color.Black.copy(alpha = 0.5f),
                    tint = if (isTorchEnabled) Color(0xFFFFD700)
                    else if (isFrontCamera) Color.White.copy(alpha = 0.3f)
                    else Color.White
                )

                // Animated flip camera button
                AnimatedIconButton(
                    onClick = onFlipCamera,
                    icon = Icons.Default.Cameraswitch
                )
            }
        }
    }
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    tint: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BottomControls(
    isCapturing: Boolean,
    recentImages: List<Uri>,
    onCapture: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    // Slide in from bottom animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated gallery preview
            if (recentImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = recentImages.take(10),
                        key = { it.toString() }
                    ) { imageUri ->
                        AnimatedGalleryImage(
                            imageUri = imageUri,
                            onClick = { onImageSelected(imageUri) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Animated capture button
            AnimatedCaptureButton(
                isCapturing = isCapturing,
                onClick = onCapture
            )
        }
    }
}

@Composable
private fun AnimatedGalleryImage(
    imageUri: Uri,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "gallery_scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Recent image",
            modifier = Modifier
                .size(60.dp)
                .scale(scale)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun AnimatedCaptureButton(
    isCapturing: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "capture_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isCapturing) 360f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "capture_rotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
            }
            .shadow(16.dp, CircleShape)
            .background(Color.White.copy(alpha = 0.9f), CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCapturing
            ) { onClick() }
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isCapturing,
            transitionSpec = {
                scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) togetherWith scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            label = "capture_content"
        ) { capturing ->
            if (capturing) {
                CircularProgressIndicator(
                    color = Color(0xFF00A884),
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

private suspend fun loadRecentImages(context: Context): List<Uri> {
    return withContext(Dispatchers.IO) {
        val images = mutableListOf<Uri>()
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    images.add(contentUri)
                    count++
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Gallery", "Error loading images: ${e.message}")
        }
        images
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private suspend fun captureImage(
    cameraController: LifecycleCameraController,
    context: Context,
    onSuccess: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val outputFile = File(
            context.cacheDir,
            "ekyc_captured_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        cameraController.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        onSuccess(uri)
                    } ?: outputFile.toUri().let { uri ->
                        onSuccess(uri)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError("Capture failed: ${exception.message}")
                }
            }
        )
    } catch (e: Exception) {
        onError("Camera error: ${e.message}")
    }
}