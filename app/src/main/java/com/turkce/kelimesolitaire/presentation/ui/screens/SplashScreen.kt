package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.turkce.kelimesolitaire.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val entranceScale = remember { Animatable(0.70f) }
    val screenAlpha = remember { Animatable(1f) }
    val shimmerProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashAura")

    // Rhythmic organic breathing pulse after entrance
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    // Ambient halo expansion pulse
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    LaunchedEffect(Unit) {
        // Step 1: Dynamic Cinematic Intro (Fade in + Pop & Settle)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            // Elegant overshoot: zooms into 1.05 then settles to 1.0
            entranceScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 850, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.15f))
            )
            entranceScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        // Step 2: Trigger visible light shimmer sweep across the letters
        delay(600)
        shimmerProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
        )

        // Step 3: Living brand hold
        delay(1000)

        // Step 4: Cinematic Fade Out into Main Menu
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    val combinedScale = entranceScale.value * breathingScale

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Luminous ambient theatrical halo behind the logo
        Box(
            modifier = Modifier
                .size(380.dp)
                .scale(auraScale)
                .alpha(logoAlpha.value * 0.9f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color(0xFF6366F1).copy(alpha = 0.10f),
                            Color(0xFF0F172A).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Studio Emblem with dynamic scale and light sweep
        Image(
            painter = painterResource(id = R.drawable.np_studio_logo),
            contentDescription = "NP Studio Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(280.dp)
                .scale(combinedScale)
                .alpha(logoAlpha.value)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    val p = shimmerProgress.value
                    if (p > 0.01f && p < 0.99f) {
                        val sweepCenter = size.width * (p * 2.2f - 0.6f)
                        val beamWidth = size.width * 0.35f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.75f),
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                start = Offset(sweepCenter - beamWidth, 0f),
                                end = Offset(sweepCenter + beamWidth, size.height)
                            ),
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
        )
    }
}
