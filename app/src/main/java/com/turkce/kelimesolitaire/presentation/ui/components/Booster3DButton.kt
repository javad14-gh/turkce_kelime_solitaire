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
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterButtonShadow
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimGradientTop
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimGradientMid
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterRimGradientBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterFaceTop
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterFaceMid
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterFaceBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterFaceBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterBadgeGreenTop
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterBadgeGreenBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.BoosterPrimaryColor
import com.turkce.kelimesolitaire.presentation.ui.theme.GameTableBg
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

enum class BoosterType {
    HINT,
    UNDO,
    JOKER
}

/**
 * 3D Juicy booster action button matching the EXACT architectural layers,
 * glossy 3-color gradient face, and bevel styling of the Level Start Button:
 * - Outer 3D Rim Shell with 3-tone gradient & border
 * - Deep 3D bottom base shadow extrusion
 * - Glossy 3-tone face pad (Top Highlight -> Mid Vibrant -> Bottom Shade)
 * - Tactile physical press response (compressing bottom bevel on click)
 * - Overlapping 3D circular/pill badge on the Bottom-Right corner
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

    // انیمیشن فشردن دکمه
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "pressOffsetY"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 10.dp,
        animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing),
        label = "shadowElevation"
    )

    // رنگ‌ها: حالت عادی مثل SquareMenuButton، حالت قفل خاکستری
    val frameColor = if (isUnlocked) BoosterPrimaryColor else Color(0xFF64748B)
    val faceBrush = if (isUnlocked) {
        Brush.verticalGradient(listOf(BoosterRimGradientTop,BoosterRimGradientMid))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
    }

    val shape = RoundedCornerShape(24.dp)
    val innerShape = RoundedCornerShape(19.dp)

    // همیشه LTR تا بج دقیقاً پایین-راست بیفته
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(contentAlignment = Alignment.BottomEnd) {

            // خود دکمه (همان ساختار SquareMenuButton)
            Box(
                modifier = Modifier
                    .offset(y = pressOffsetY)
                    .size(80.dp)
                    .shadow(shadowElevation, shape)
                    .clip(shape)
                    .background(frameColor)
                    .candyGloss(corner = 24.dp, inset = 5.dp)
                    .padding(8.dp)
                    .clip(innerShape)
                    .background(faceBrush)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = if (!isUnlocked) Modifier.alpha(0.35f) else Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                if (!isUnlocked) {
                    Text(
                        text = "🔒",
                        fontSize = 24.sp,
                        modifier = Modifier.zIndex(2f)
                    )
                }
            }

            // بج گوشه پایین-راست (بدون تغییر)
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
                            modifier = Modifier.size(24.dp),
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
