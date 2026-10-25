package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackGradientBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackGradientTop
import com.turkce.kelimesolitaire.presentation.ui.theme.maincolor4
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.turkce.kelimesolitaire.R
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.mainBg


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
            .background(mainBg),
        contentAlignment = Alignment.Center
    ) {
        // Center card container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // 3D Playing Card Icon
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(360.dp)
                    .rotate(cardRotation)
                    .scale(cardScale)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Level Title
            OutlinedText(
                text = title,
                textColor = Color.White,
                outlineColor = Color(0xFF1E0048),
                outlineWidth = 6f,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )


            Spacer(modifier = Modifier.height(35.dp))

            // Chunky 3D progress bar (button-style: gold rim + bottom bevel + glossy fill)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    // Outer gold rim
                    .background(Color(0xFFFFD21F))
                    .padding(3.5.dp)
                    .clip(RoundedCornerShape(17.dp))
                    // Bottom bevel base (3D thickness)
                    .background(Color(0xFF06813E))
                    //.padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    // Dark track
                    .background(Color(0xFF06813E))
                    .height(15.dp)
            ) {
                // Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF00BF37))
                ) {
                    // Glossy highlight on top half
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.30f))
                    )
                }

                // Percentage inside the bar
                val percentInBar = (progressFraction * 100).toInt().coerceIn(0, 100)
                OutlinedText(
                    text = "${LocaleHelper.formatNumber(percentInBar, isPersian)}%",
                    modifier = Modifier.align(Alignment.Center),
                    textColor = Color.White,
                    outlineColor = Color(0xFF14532D),
                    outlineWidth = 4f,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    shadowOffsetY = 1f
                )
            }

        }
    }
}
