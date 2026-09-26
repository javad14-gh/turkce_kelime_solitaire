package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

/**
 * High-performance, cinematic Game Logo Progress Bar.
 * Fills up the stylized "پاسور کلمات" artwork from left to right (progress style).
 * 
 * - Bottom layer: Grayscale / desaturated empty silhouette
 * - Top layer: Full vibrant color artwork, clipped left-to-right by [progress] (0f..1f)
 * - Trailing edge: Luminous laser shimmer sweep that rides the fill frontier
 */
@Composable
fun LogoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isPersian: Boolean = true,
    showGlow: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "LogoProgressAura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Ambient rhythmic glow aura behind the logo
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(240.dp, 80.dp)
                    .scale(1.15f)
                    .alpha(auraAlpha * (0.3f + clampedProgress * 0.7f))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeon.copy(alpha = 0.45f),
                                AccentGold.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        if (isPersian) {
            // Persian Mode: Stylized Graphic Title Logo (1024 x 252 -> Aspect Ratio 4.06)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1024f / 252f),
                contentAlignment = Alignment.Center
            ) {
                // 1. Unfilled Background Layer: Grayscale / Dimmed silhouette
                Image(
                    painter = painterResource(id = R.drawable.title_logo),
                    contentDescription = "Logo Background Unfilled",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.26f),
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }
                    ),
                    contentScale = ContentScale.FillBounds
                )

                // 2. Active Foreground Layer: Full color, clipped from Left to Right
                if (clampedProgress > 0.001f) {
                    Image(
                        painter = painterResource(id = R.drawable.title_logo),
                        contentDescription = "Logo Active Fill",
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val fillWidth = size.width * clampedProgress

                                // Clip active content to progress boundary
                                clipRect(left = 0f, top = 0f, right = fillWidth, bottom = size.height) {
                                    this@drawWithContent.drawContent()
                                }

                                // Glowing energy sweep beam at the leading frontier
                                if (clampedProgress in 0.015f..0.985f) {
                                    val beamWidth = 24.dp.toPx()
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFFFFD700).copy(alpha = 0.35f),
                                                Color.White.copy(alpha = 0.95f),
                                                Color(0xFFC084FC).copy(alpha = 0.75f),
                                                Color.Transparent
                                            ),
                                            startX = fillWidth - beamWidth,
                                            endX = fillWidth + beamWidth
                                        ),
                                        topLeft = Offset(fillWidth - beamWidth, 0f),
                                        size = Size(beamWidth * 2f, size.height),
                                        blendMode = BlendMode.SrcAtop
                                    )
                                }
                            },
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        } else {
            // Turkish Mode Fallback: Stylized Outlined Text with same LTR clipping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dimmed background
                OutlinedText(
                    text = "KELİME SOLİTAİRE",
                    textColor = Color.White.copy(alpha = 0.25f),
                    outlineColor = Color(0xFF0F172A).copy(alpha = 0.5f),
                    outlineWidth = 5f,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                // Active foreground clip
                if (clampedProgress > 0.001f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                val fillWidth = size.width * clampedProgress
                                clipRect(left = 0f, top = 0f, right = fillWidth, bottom = size.height) {
                                    this@drawWithContent.drawContent()
                                }

                                if (clampedProgress in 0.015f..0.985f) {
                                    val beamWidth = 20.dp.toPx()
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.95f),
                                                Color(0xFF38BDF8).copy(alpha = 0.75f),
                                                Color.Transparent
                                            ),
                                            startX = fillWidth - beamWidth,
                                            endX = fillWidth + beamWidth
                                        ),
                                        topLeft = Offset(fillWidth - beamWidth, 0f),
                                        size = Size(beamWidth * 2f, size.height),
                                        blendMode = BlendMode.SrcAtop
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = "KELİME SOLİTAİRE",
                            textColor = SecondaryNeon,
                            outlineColor = Color(0xFF0F172A),
                            outlineWidth = 5f,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
