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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterButtonDark
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterButtonHighlight
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterButtonShadow
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimBase
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterBadgeGreenTop
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterBadgeGreenBottom
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

enum class BoosterType {
    HINT,
    UNDO,
    JOKER
}

/**
 * Juicy, arcade-style 3D booster action button styled directly after the reference design:
 * - Rounded squircle shape with vibrant cyan/sky-blue 3D rim and bottom pedestal
 * - Deep royal blue / violet glossy pad with top reflection arc
 * - Prominent centered booster icon
 * - Overlapping 3D circular/pill badge on the BOTTOM-RIGHT with thick white border
 *   (showing available count or coin cost)
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
        targetValue = if (isPressed && enabled) 1.5.dp else 4.5.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "bevelPadding"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 7.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "shadowElevation"
    )

    // Palette per booster state:
    // Cyan Outer Base + Royal Indigo Face (matching screenshot)
    val (rimBrush, rimBorderColor, baseExtrusionColor, faceBrush, faceHighlightBorder) = when {
        !isUnlocked -> Quintuple(
            Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
            Color(0xFF94A3B8).copy(alpha = 0.4f),
            Color(0xFF0F172A),
            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
            Color(0xFF475569).copy(alpha = 0.3f)
        )
        else -> Quintuple(
            // Electric Cyan Outer Rim & Pedestal
            Brush.verticalGradient(listOf(BoosterRimBorder, BoosterRimBase, BoosterButtonShadow)),
            BoosterRimBorder,
            BoosterButtonShadow, // 3D bottom extrusion lip
            // Vibrant Royal Blue / Indigo Face
            Brush.verticalGradient(listOf(BoosterButtonHighlight, BoosterButtonBg, BoosterButtonDark)),
            Color(0xFF818CF8).copy(alpha = 0.60f)
        )
    }

    // Always use LTR for the button container so the badge is reliably at physical Bottom-Right
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // 3D Juicy Button Shell
            Box(
                modifier = Modifier
                    .offset(y = pressOffsetY)
                    .shadow(shadowElevation, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    // Layer 1: Outer 3D Cyan Rim & Pedestal
                    .background(rimBrush)
                    .border(1.5.dp, rimBorderColor, RoundedCornerShape(18.dp))
                    .padding(2.5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    // Layer 2: 3D Base Extrusion Shadow at bottom
                    .background(baseExtrusionColor)
                    .padding(bottom = bevelPadding)
                    .clip(RoundedCornerShape(14.dp))
                    // Layer 3: Main Vibrant Indigo Face
                    .background(faceBrush)
                    .border(1.2.dp, faceHighlightBorder, RoundedCornerShape(14.dp))
                    .size(66.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Layer 4: Glossy glass curvature reflection on upper half (matching screenshot)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .align(Alignment.TopCenter)
                        .clip(
                            RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
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

                // If locked: Big 3D golden padlock overlay centered on button
                if (!isUnlocked) {
                    Text(
                        text = "🔒",
                        fontSize = 24.sp,
                        modifier = Modifier.zIndex(2f)
                    )
                }
            }

            // Layer 6: Overlapping 3D Badge on Bottom-Right (matching green '+' badge in screenshot)
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = 6.dp + pressOffsetY)
                    .zIndex(4f)
                    .shadow(4.dp, if (!isUnlocked || (hasFree && freeCount > 0)) CircleShape else RoundedCornerShape(12.dp))
                    .clip(if (!isUnlocked || (hasFree && freeCount > 0)) CircleShape else RoundedCornerShape(12.dp))
                    .background(
                        when {
                            !isUnlocked -> Brush.horizontalGradient(
                                listOf(Color(0xFF64748B), Color(0xFF475569))
                            )
                            else -> Brush.verticalGradient(
                                listOf(BoosterBadgeGreenTop, BoosterBadgeGreenBottom)
                            )
                        }
                    )
                    .border(
                        1.5.dp,
                        Color.White,
                        if (!isUnlocked || (hasFree && freeCount > 0)) CircleShape else RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !isUnlocked -> {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lockText,
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                    }
                    hasFree && freeCount > 0 -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LocaleHelper.formatNumber(freeCount, isPersian),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = fontFamily
                            )
                        }
                    }
                    else -> {
                        // Juicy Green 3D Badge with mini Coin + Cost (e.g. 🪙 50)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 5.dp, end = 7.dp, top = 2.dp, bottom = 2.dp)
                        ) {
                            CoinIcon(size = 14.dp)
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = LocaleHelper.formatNumber(coinCost, isPersian),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = fontFamily
                            )
                        }
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
