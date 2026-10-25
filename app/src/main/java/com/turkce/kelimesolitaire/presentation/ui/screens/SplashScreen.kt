package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    isPersian: Boolean = LocaleHelper.isPersian(LocalContext.current),
    onSplashFinished: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()

    // Phase 1: Studio Splash animatables
    val studioLogoAlpha = remember { Animatable(0f) }
    val studioEntranceScale = remember { Animatable(0.70f) }
    val studioShimmerProgress = remember { Animatable(0f) }

    // Phase 2: Game Loading animatables
    val gameLoadingAlpha = remember { Animatable(0f) }
    val gameProgress = remember { Animatable(0.04f) }

    // Global fade out into Main Menu
    val screenAlpha = remember { Animatable(1f) }

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
        // --- STAGE 1: Studio Logo Cinematic Intro ---
        launch {
            studioLogoAlpha.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }
        launch {
            studioEntranceScale.animateTo(
                1.05f,
                tween(700, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.15f))
            )
            studioEntranceScale.animateTo(1.0f, tween(300, easing = FastOutSlowInEasing))
        }

        delay(400)
        studioShimmerProgress.animateTo(1f, tween(850, easing = LinearEasing))
        delay(350)

        studioLogoAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
        onSplashFinished()
    }

    val combinedStudioScale = studioEntranceScale.value * breathingScale

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Color.Black)
            .clickable {
                // Allow tapping to proceed immediately
                onSplashFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        // --- Layer 1: Studio Splash ---
        if (studioLogoAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(studioLogoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Luminous ambient theatrical halo behind the logo
                Box(
                    modifier = Modifier
                        .size(380.dp)
                        .scale(auraScale)
                        .alpha(0.85f)
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
                        .scale(combinedStudioScale)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            val p = studioShimmerProgress.value
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
    }
}
