package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

enum class BoosterType {
    HINT,
    UNDO,
    JOKER
}

/**
 * Juicy, arcade-style 3D booster action button.
 * Features:
 * - Thick bottom extrusion bevel (5dp) with deep shadow
 * - Distinctive color palette per booster (Electric Cyan, Cosmic Purple, 24K Gold)
 * - Glossy curved highlight reflection on upper half
 * - Physical tactile depression on press (3.5dp down)
 * - Overlapping 3D pill badge for lock, free gift, or coin cost
 */
@Composable
fun Booster3DButton(
    type: BoosterType,
    isUnlocked: Boolean,
    lockText: String,
    hasFree: Boolean,
    freeCount: Int,
    coinCost: Int,
    isPersian: Boolean,
    enabled: Boolean,
    fontFamily: FontFamily?,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press animation states
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "pressOffsetY"
    )
    val bevelPadding by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.5.dp else 5.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "bevelPadding"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 9.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "shadowElevation"
    )

    // Palette per booster type (or muted slate if locked)
    val (rimBrush, rimBorderColor, baseShadowColor, faceBrush, faceHighlightBorder) = when {
        !isUnlocked -> Quintuple(
            Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
            Color(0xFF94A3B8).copy(alpha = 0.35f),
            Color(0xFF0F172A),
            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
            Color(0xFF475569).copy(alpha = 0.3f)
        )
        type == BoosterType.HINT -> Quintuple(
            // Electric Sky Blue / Cyan
            Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7))),
            Color(0xFFBAE6FD).copy(alpha = 0.85f),
            Color(0xFF0C4A6E), // Deep midnight blue 3D base
            Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))),
            Color(0xFFBAE6FD).copy(alpha = 0.65f)
        )
        type == BoosterType.UNDO -> Quintuple(
            // Cosmic Royal Violet
            Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFA855F7), Color(0xFF7C3AED))),
            Color(0xFFDDD6FE).copy(alpha = 0.85f),
            Color(0xFF3B0764), // Deep midnight violet 3D base
            Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF581C87))),
            Color(0xFFF3E8FF).copy(alpha = 0.65f)
        )
        else -> Quintuple(
            // 24K Gold & Flame Amber
            Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFF59E0B))),
            Color(0xFFFEF08A).copy(alpha = 0.95f),
            Color(0xFF78350F), // Deep bronze 3D base
            Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309))),
            Color(0xFFFEF08A).copy(alpha = 0.8f)
        )
    }

    Box(contentAlignment = Alignment.TopStart) {
        // 3D Juicy Button Shell
        Box(
            modifier = Modifier
                .offset(y = pressOffsetY)
                .shadow(shadowElevation, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                // Layer 1: Outer 3D Rim (glossy metallic/crystal bevel shell)
                .background(rimBrush)
                .border(1.5.dp, rimBorderColor, RoundedCornerShape(20.dp))
                .padding(2.5.dp)
                .clip(RoundedCornerShape(17.dp))
                // Layer 2: 3D Base Extrusion Shadow
                .background(baseShadowColor)
                .padding(bottom = bevelPadding)
                .clip(RoundedCornerShape(15.dp))
                // Layer 3: Main Vibrant Face
                .background(faceBrush)
                .border(1.2.dp, faceHighlightBorder, RoundedCornerShape(15.dp))
                .size(68.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Layer 4: Glossy glass curvature reflection on upper half
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.TopCenter)
                    .clip(
                        RoundedCornerShape(
                            topStart = 15.dp,
                            topEnd = 15.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isUnlocked) 0.38f else 0.12f),
                                Color.White.copy(alpha = if (isUnlocked) 0.08f else 0.02f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Layer 5: Centered Icon
            Box(
                modifier = if (!isUnlocked) Modifier.alpha(0.35f) else Modifier,
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }

        // Layer 6: Overlapping 3D Pill Badge on Top-Start
        Box(
            modifier = Modifier
                .offset(x = (-7).dp, y = (-7).dp + pressOffsetY)
                .zIndex(3f)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    when {
                        !isUnlocked -> Brush.horizontalGradient(
                            listOf(Color(0xFF64748B), Color(0xFF475569))
                        )
                        hasFree && freeCount > 0 -> Brush.horizontalGradient(
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                        else -> Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFD97706))
                        )
                    }
                )
                .border(1.5.dp, Color.White, CircleShape)
                .padding(horizontal = 6.5.dp, vertical = 2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !isUnlocked -> {
                    Text(
                        text = lockText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                }
                hasFree && freeCount > 0 -> {
                    Text(
                        text = if (freeCount > 1) {
                            if (isPersian) "🎁 ${LocaleHelper.formatNumber(freeCount, true)} عدد" else "🎁 ${freeCount}×"
                        } else {
                            if (isPersian) "🎁 رایگان" else "🎁 Ücretsiz"
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = fontFamily
                    )
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CoinIcon(size = 15.dp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = LocaleHelper.formatNumber(coinCost, isPersian),
                            color = Color(0xFF0F172A),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
