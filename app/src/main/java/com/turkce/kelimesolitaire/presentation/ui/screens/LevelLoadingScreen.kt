package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.heightIn
import com.turkce.kelimesolitaire.presentation.ui.components.LogoProgressBar
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackGradientBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackGradientTop
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

@Composable
fun LevelLoadingScreen(
    levelNumber: Int,
    modifier: Modifier = Modifier,
    isPersian: Boolean = LocaleHelper.isPersian(LocalContext.current)
) {
    val nunitoFont = rememberNunitoFont()
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingAnimation")

    // Gentle card breathing & tilt animation
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CardPulse"
    )
    val cardRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CardTilt"
    )

    // Progressive loading fill (always fills up steadily, never reverses or drains!)
    val progressAnim = remember { androidx.compose.animation.core.Animatable(0.04f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 0.45f,
            animationSpec = tween(320, easing = FastOutSlowInEasing)
        )
        progressAnim.animateTo(
            targetValue = 0.88f,
            animationSpec = tween(480, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
        )
        progressAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    }
    val progressFraction = progressAnim.value

    // Shimmer sweep across progress bar
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerSweep"
    )

    val title = if (levelNumber > 0) {
        LocaleHelper.levelTitle(levelNumber, isPersian)
    } else {
        if (isPersian) "پاسور کلمات" else "Kelime Solitaire"
    }

    val subtitle = if (isPersian) {
        "در حال چیدمان و بر زدن کارت‌ها..."
    } else {
        "Kartlar diziliyor ve karıştırılıyor..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E5E3A), Color(0xFF0A2616))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle decorative background glows
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(cardScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x33A3E635), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Center card container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // 3D Playing Card Icon
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 98.dp)
                    .rotate(cardRotation)
                    .scale(cardScale)
                    .shadow(16.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                        )
                    )
                    .border(2.dp, Color(0xFFFBBF24), RoundedCornerShape(12.dp))
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Card Inner Pattern
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(CardBackGradientTop, CardBackGradientBottom)
                            )
                        )
                        .border(1.dp, Color(0x66FBBF24), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♠",
                        fontSize = 32.sp,
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Level Title
            OutlinedText(
                text = title,
                textColor = Color.White,
                outlineColor = Color(0xFF052E16),
                outlineWidth = 6f,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle text
            Text(
                text = subtitle,
                color = Color(0xFFD1FAE5),
                fontSize = 15.sp,
                fontFamily = nunitoFont,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))
            
            // Stylized Game Logo Progress Bar with Glowing Frontier Beam
            LogoProgressBar(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 80.dp),
                isPersian = isPersian,
                showGlow = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage Counter
            val percent = (progressFraction * 100).toInt().coerceIn(0, 100)
            Text(
                text = "${LocaleHelper.formatNumber(percent, isPersian)}%",
                color = AccentGold,
                fontSize = 17.sp,
                fontFamily = nunitoFont,
                fontWeight = FontWeight.Black
            )
        }
    }
}
