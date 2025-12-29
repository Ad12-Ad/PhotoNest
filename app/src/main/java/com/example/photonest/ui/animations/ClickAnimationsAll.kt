package com.example.photonest.ui.animations

/**
 * AnimationModifierExtensions.kt
 *
 * Production-ready Modifier extension functions for Android Jetpack Compose animations
 * Optimized for performance and reusability
 *
 * Package: com.raavienergy.electrician.animations
 *
 * USAGE:
 * import com.raavienergy.electrician.animations.*
 *
 * Text("Click me", modifier = Modifier.bouncyClick { handleClick() })
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// ================================================================================================
// 1. CLICK & PRESS ANIMATIONS
// ================================================================================================

/**
 * Bouncy click animation with three-stage effect
 *
 * WHEN TO USE:
 * - Primary buttons and CTAs
 * - Interactive cards
 * - Custom clickable components
 * - Game UI elements
 *
 * VISUAL: Press (92%) → Bounce (106%) → Rest (100%)
 * Creates satisfying tactile feedback
 *
 * @param enabled Whether click is enabled
 * @param pressedScale Scale when pressed (default: 0.92f = 92%)
 * @param bounceScale Peak bounce scale (default: 1.06f = 106%)
 * @param onClick Click handler
 *
 * @example
 * Box(
 *     modifier = Modifier.bouncyClick { viewModel.submitForm() }
 * ) {
 *     Text("Submit")
 * }
 */
//fun Modifier.bouncyClick(
//    enabled: Boolean = true,
//    pressedScale: Float = 0.92f,
//    bounceScale: Float = 1.06f,
//    onClick: () -> Unit = {}
//) = composed {
//    val scale = remember { Animatable(1f) }
//    val scope = rememberCoroutineScope()
//
//    this
//        .graphicsLayer {
//            scaleX = scale.value
//            scaleY = scale.value
//        }
//        .clickable(
//            enabled = enabled,
//            indication = null,
//            interactionSource = remember { MutableInteractionSource() }
//        ) {
//            scope.launch {
//                // Stage 1: Press down
//                scale.animateTo(
//                    pressedScale,
//                    animationSpec = tween(90)
//                )
//
//                // Stage 2: Bounce up
//                scale.animateTo(
//                    bounceScale,
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
//                )
//
//                // Stage 3: Settle to normal
//                scale.animateTo(
//                    1f,
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioNoBouncy,
//                        stiffness = Spring.StiffnessMedium
//                    )
//                )
//
//                onClick()
//            }
//        }
//}

/**
 * Simple press animation (scale down on press, spring back)
 *
 * WHEN TO USE:
 * - List items
 * - Settings options
 * - Menu items
 * - Subtle interactions
 *
 * VISUAL: Scales to 95% on press, springs back to 100%
 * Lighter than bouncyClick for frequent interactions
 *
 * @param pressScale Scale value when pressed
 * @param onClick Click handler
 */
//fun Modifier.pressClick(
//    enabled: Boolean = true,
//    pressScale: Float = 0.95f,
//    onClick: () -> Unit = {}
//) = composed {
//    val scale = remember { Animatable(1f) }
//    val scope = rememberCoroutineScope()
//
//    this
//        .graphicsLayer {
//            scaleX = scale.value
//            scaleY = scale.value
//        }
//        .clickable(
//            enabled = enabled,
//            indication = null,
//            interactionSource = remember { MutableInteractionSource() }
//        ) {
//            scope.launch {
//                scale.animateTo(pressScale, tween(100))
//                scale.animateTo(
//                    1f,
//                    spring(
//                        dampingRatio = Spring.DampingRatioMediumBouncy,
//                        stiffness = Spring.StiffnessMediumLow
//                    )
//                )
//                onClick()
//            }
//        }
//}

/**
 * Long press animation with growing scale
 *
 * WHEN TO USE:
 * - Hold-to-confirm actions
 * - Voice recording buttons
 * - Delete confirmations
 * - Context menus
 *
 * VISUAL: Gradually grows to 110% while holding
 */
fun Modifier.longPressGrow(
    enabled: Boolean = true,
    targetScale: Float = 1.1f,
    onLongPress: () -> Unit
) = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(
                targetScale,
                animationSpec = tween(600, easing = LinearOutSlowInEasing)
            )
            onLongPress()
        } else {
            scale.animateTo(1f, spring())
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

// ================================================================================================
// 2. SHAKE & ERROR ANIMATIONS
// ================================================================================================

/**
 * Shake animation for error feedback
 *
 * WHEN TO USE:
 * - Form validation errors
 * - Wrong password
 * - Invalid input
 * - Payment failures
 * - Permission denied
 *
 * VISUAL: Rapid horizontal shake (left-right-left-right)
 *
 * @param trigger Change this value to trigger shake
 * @param strength Shake distance in pixels
 * @param duration Total shake duration
 */
fun Modifier.shake(
    trigger: Boolean,
    strength: Float = 15f,
    duration: Int = 400
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "shake"
        properties["trigger"] = trigger
        properties["strength"] = strength
    }
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        if (trigger) {
            scope.launch {
                // Shake sequence: 8 oscillations
                repeat(8) { iteration ->
                    offsetX.animateTo(
                        targetValue = if (iteration % 2 == 0) strength else -strength,
                        animationSpec = tween(50, easing = LinearEasing)
                    )
                }
                // Return to center
                offsetX.animateTo(0f, tween(50))
            }
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * Vertical bounce for success/error feedback
 *
 * WHEN TO USE:
 * - Success confirmations
 * - Item added to cart
 * - Achievement unlocked
 * - Notification received
 *
 * VISUAL: Bounces up and down with spring physics
 *
 * @param trigger Change to trigger bounce
 * @param bounceHeight How high to bounce (negative = up)
 */
fun Modifier.bounce(
    trigger: Boolean,
    bounceHeight: Float = -40f
) = composed {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        if (trigger) {
            scope.launch {
                // Bounce up
                offsetY.animateTo(
                    bounceHeight,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
                // Bounce down with spring
                offsetY.animateTo(
                    0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    this.graphicsLayer {
        translationY = offsetY.value
    }
}

// ================================================================================================
// 3. FADE ANIMATIONS
// ================================================================================================

/**
 * Animated fade in/out
 *
 * WHEN TO USE:
 * - Show/hide elements
 * - Conditional UI
 * - Loading overlays
 * - Modal backgrounds
 *
 * VISUAL: Smooth opacity transition 0% ↔ 100%
 *
 * @param visible Controls visibility
 * @param durationMillis Animation duration
 */
fun Modifier.animateFade(
    visible: Boolean,
    durationMillis: Int = 300
) = composed {
    val alpha = remember { Animatable(if (visible) 1f else 0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        scope.launch {
            alpha.animateTo(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = if (visible) LinearOutSlowInEasing else FastOutLinearInEasing
                )
            )
        }
    }

    this.graphicsLayer { this.alpha = alpha.value }
}

/**
 * Fade with scale (zoom in/out effect)
 *
 * WHEN TO USE:
 * - Dialog appearances
 * - Card reveals
 * - Modal popups
 * - Emphasis effects
 *
 * VISUAL: Fades while scaling from 80% to 100%
 *
 * @param visible Controls visibility
 * @param scaleFrom Starting scale
 */
fun Modifier.fadeScale(
    visible: Boolean,
    scaleFrom: Float = 0.8f,
    durationMillis: Int = 300
) = composed {
    val alpha = remember { Animatable(if (visible) 1f else 0f) }
    val scale = remember { Animatable(if (visible) 1f else scaleFrom) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        scope.launch {
            if (visible) {
                launch {
                    alpha.animateTo(
                        1f,
                        tween(durationMillis, easing = LinearOutSlowInEasing)
                    )
                }
                launch {
                    scale.animateTo(
                        1f,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
            } else {
                launch {
                    alpha.animateTo(
                        0f,
                        tween(durationMillis, easing = FastOutLinearInEasing)
                    )
                }
                launch {
                    scale.animateTo(
                        scaleFrom,
                        tween(durationMillis, easing = FastOutLinearInEasing)
                    )
                }
            }
        }
    }

    this.graphicsLayer {
        this.alpha = alpha.value
        scaleX = scale.value
        scaleY = scale.value
    }
}

// ================================================================================================
// 4. ROTATION ANIMATIONS
// ================================================================================================

/**
 * Infinite rotation (loading spinner)
 *
 * WHEN TO USE:
 * - Loading indicators
 * - Refresh icons
 * - Sync/upload states
 * - Progress spinners
 *
 * VISUAL: Continuous 360° rotation
 *
 * @param enabled Controls rotation
 * @param durationMillis Speed of rotation
 * @param clockwise Rotation direction
 */
fun Modifier.rotateInfinitely(
    enabled: Boolean = true,
    durationMillis: Int = 1000,
    clockwise: Boolean = true
) = composed {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(enabled) {
        if (enabled) {
            scope.launch {
                while (true) {
                    rotation.animateTo(
                        targetValue = if (clockwise) 360f else -360f,
                        animationSpec = tween(durationMillis, easing = LinearEasing)
                    )
                    rotation.snapTo(0f)
                }
            }
        } else {
            rotation.animateTo(0f, tween(200))
        }
    }

    this.rotate(rotation.value)
}

/**
 * Rotate on expand/collapse (dropdown arrows)
 *
 * WHEN TO USE:
 * - Expandable sections
 * - Dropdown menus
 * - Accordion items
 * - FAQ sections
 *
 * VISUAL: Rotates 180° when expanded (arrow down → up)
 *
 * @param expanded Controls rotation state
 */
fun Modifier.rotateOnExpand(
    expanded: Boolean,
    durationMillis: Int = 300
) = composed {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded) {
        scope.launch {
            rotation.animateTo(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
            )
        }
    }

    this.rotate(rotation.value)
}

/**
 * Single rotation animation (flip effect)
 *
 * WHEN TO USE:
 * - Card flips
 * - Reveal animations
 * - Toggle states
 *
 * @param trigger Change to trigger rotation
 * @param degrees Rotation amount
 */
fun Modifier.rotateOnce(
    trigger: Boolean,
    degrees: Float = 360f,
    durationMillis: Int = 600
) = composed {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        if (trigger) {
            scope.launch {
                rotation.animateTo(
                    degrees,
                    tween(durationMillis, easing = FastOutSlowInEasing)
                )
                rotation.snapTo(0f)
            }
        }
    }

    this.rotate(rotation.value)
}

// ================================================================================================
// 5. SCALE ANIMATIONS
// ================================================================================================

/**
 * Pulse animation (breathing effect)
 *
 * WHEN TO USE:
 * - Notification badges
 * - New feature indicators
 * - Recording states
 * - Important CTAs
 *
 * VISUAL: Continuously breathes 100% → 110% → 100%
 *
 * @param enabled Controls pulsing
 * @param targetScale Maximum scale
 * @param durationMillis Pulse cycle duration
 */
fun Modifier.pulse(
    enabled: Boolean = true,
    targetScale: Float = 1.1f,
    durationMillis: Int = 1000
) = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(enabled) {
        if (enabled) {
            scope.launch {
                while (true) {
                    scale.animateTo(
                        targetScale,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                    scale.animateTo(
                        1f,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
            }
        } else {
            scale.animateTo(1f, tween(200))
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Heartbeat animation (like button)
 *
 * WHEN TO USE:
 * - Like/favorite buttons
 * - Health metrics
 * - Live indicators
 *
 * VISUAL: Double pulse (ba-bump, ba-bump)
 *
 * @param enabled Controls heartbeat
 */
fun Modifier.heartbeat(
    enabled: Boolean = true
) = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(enabled) {
        if (enabled) {
            scope.launch {
                while (true) {
                    // First beat
                    scale.animateTo(1.2f, tween(100))
                    scale.animateTo(1f, tween(100))
                    delay(100)
                    // Second beat
                    scale.animateTo(1.15f, tween(80))
                    scale.animateTo(1f, tween(80))
                    delay(800) // Pause before next heartbeat
                }
            }
        } else {
            scale.animateTo(1f, tween(200))
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Pop-in animation (single scale burst)
 *
 * WHEN TO USE:
 * - Item added confirmations
 * - Badge updates
 * - Counter increments
 *
 * @param trigger Change to trigger pop
 */
fun Modifier.popIn(
    trigger: Any?
) = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        scope.launch {
            scale.animateTo(1.3f, tween(150))
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

// ================================================================================================
// 6. SHIMMER EFFECT
// ================================================================================================

/**
 * Shimmer loading effect (skeleton screens)
 *
 * WHEN TO USE:
 * - Content loading placeholders
 * - Image loading
 * - List loading states
 * - Network fetching
 *
 * VISUAL: Animated gradient sweeps left to right
 *
 * @param enabled Controls shimmer
 * @param durationMillis Shimmer sweep speed
 */
fun Modifier.shimmer(
    enabled: Boolean = true,
    durationMillis: Int = 1200
) = composed {
    if (!enabled) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    this.drawBehind {
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, translateAnim),
            end = Offset(translateAnim + 200f, translateAnim + 200f)
        )
        drawRect(brush = brush)
    }
}

// ================================================================================================
// 7. SLIDE ANIMATIONS
// ================================================================================================

/**
 * Slide in from edge
 *
 * WHEN TO USE:
 * - Bottom sheets
 * - Side menus
 * - Notifications
 * - New messages
 *
 * VISUAL: Slides in from specified direction
 *
 * @param visible Controls visibility
 * @param fromEdge Direction to slide from
 * @param distance Slide distance
 */
fun Modifier.slideIn(
    visible: Boolean,
    fromEdge: SlideEdge = SlideEdge.Bottom,
    distance: Dp = 300.dp,
    durationMillis: Int = 300
) = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        scope.launch {
            when (fromEdge) {
                SlideEdge.Start -> {
                    offsetX.snapTo(if (visible) -distance.value else 0f)
                    offsetX.animateTo(
                        if (visible) 0f else -distance.value,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
                SlideEdge.End -> {
                    offsetX.snapTo(if (visible) distance.value else 0f)
                    offsetX.animateTo(
                        if (visible) 0f else distance.value,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
                SlideEdge.Top -> {
                    offsetY.snapTo(if (visible) -distance.value else 0f)
                    offsetY.animateTo(
                        if (visible) 0f else -distance.value,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
                SlideEdge.Bottom -> {
                    offsetY.snapTo(if (visible) distance.value else 0f)
                    offsetY.animateTo(
                        if (visible) 0f else distance.value,
                        tween(durationMillis, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    this.graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
    }
}

enum class SlideEdge {
    Start, End, Top, Bottom
}

// ================================================================================================
// 8. WAVE & OSCILLATION
// ================================================================================================

/**
 * Wave animation (sine wave motion)
 *
 * WHEN TO USE:
 * - Audio recording
 * - Voice input
 * - Water/liquid effects
 * - Floating animations
 *
 * VISUAL: Vertical sine wave oscillation
 *
 * @param enabled Controls wave
 */
fun Modifier.wave(
    enabled: Boolean = true,
    amplitude: Float = 10f,
    durationMillis: Int = 2000
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    if (enabled) {
        this.graphicsLayer {
            translationY = sin(Math.toRadians(waveOffset.toDouble())).toFloat() * amplitude
        }
    } else {
        this
    }
}

// ================================================================================================
// 9. GLOW EFFECT
// ================================================================================================

/**
 * Pulsing glow effect
 *
 * WHEN TO USE:
 * - Premium features
 * - Selected states
 * - Live indicators
 * - Special promotions
 *
 * VISUAL: Pulsing colored aura
 *
 * @param enabled Controls glow
 * @param color Glow color
 */
fun Modifier.glow(
    enabled: Boolean,
    color: Color = Color.Yellow,
    pulseSpeed: Int = 1500
) = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.maxDimension * 0.6f
        )
    }
}

// ================================================================================================
// USAGE EXAMPLES (Commented out - uncomment to test)
// ================================================================================================

/*
@Preview(showBackground = true)
@Composable
fun AnimationPreview() {
    var trigger by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bouncy Click
        Button(
            onClick = {},
            modifier = Modifier.bouncyClick {
                println("Clicked with bounce!")
            }
        ) {
            Text("Bouncy Button")
        }

        // Shake on error
        var hasError by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.shake(trigger = hasError),
            label = { Text("Email") }
        )
        Button(onClick = { hasError = !hasError }) {
            Text("Trigger Shake")
        }

        // Pulse notification
        Badge(
            modifier = Modifier.pulse()
        ) {
            Text("5")
        }

        // Shimmer loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shimmer(enabled = true)
        )

        // Rotate infinitely
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.rotateInfinitely()
        )
    }
}
*/