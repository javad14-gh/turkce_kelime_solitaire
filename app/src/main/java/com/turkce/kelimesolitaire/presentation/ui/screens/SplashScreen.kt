package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val logoScale = remember { Animatable(0.88f) }
    val screenAlpha = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashShimmer")

    // Ambient gold breathing glow behind studio logo
    val ambientGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AmbientGlowScale"
    )

    LaunchedEffect(Unit) {
        // Step 1: Smooth Logo Intro (Fade + Zoom In)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
        }

        // Step 2: Pure studio branding presence (while db and ads initialize in background)
        delay(1900)

        // Step 3: Cinematic Fade Out into Main Menu
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF16181F),
                        Color(0xFF0C0E13),
                        Color(0xFF07080B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Golden Glow behind logo
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(ambientGlowScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x35E5B94E),
                            Color(0x15D4AF37),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Studio Emblem (Pure AAA Floating Logo)
        Image(
            painter = painterResource(id = R.drawable.np_studio_logo),
            contentDescription = "NP Studio Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(250.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
        )
    }
}
