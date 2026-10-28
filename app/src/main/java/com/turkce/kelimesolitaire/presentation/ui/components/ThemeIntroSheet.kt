package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTitleOutline
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

/**
 * Transparent-backdrop bottom sheet: the real game board stays visible above it,
 * so the user sees the theme change live while tapping the color circles.
 */
@Composable
fun ThemeIntroSheet(
    rewardCoins: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val font = rememberNunitoFont()
    var hasPicked by remember { mutableStateOf(false) }

    BackHandler { onDismiss() }

    val pulse = rememberInfiniteTransition(label = "themeSheetPulse")
    val btnScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "btnScale"
    )

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, sheetShape)
            .clip(sheetShape)
            .background(PopupBg)
            .border(2.5.dp, DarkBg, sheetShape)
            // swallow touches so they don't reach the board behind the sheet
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedText(
                text = if (isPersian) "رنگ بازی رو انتخاب کن! 🎨" else "Oyun Rengini Seç! 🎨",
                textColor = Color.White,
                outlineColor = PopupTitleOutline,
                outlineWidth = 10f,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isPersian)
                    "یکی از رنگ‌ها رو بزن و تغییرش رو روی میز و کارت‌ها ببین!"
                else
                    "Bir renge dokun ve masadaki, kartlardaki değişimi hemen gör!",
                color = PopupTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = font,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            ThemeSelectorSection(
                isPersian = isPersian,
                fontFamily = font,
                onThemeSelected = { hasPicked = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (hasPicked) {
                CandyButton(
                    onClick = {
                        GameSettingsManager.playButtonClickSound(context)
                        onConfirm()
                    },
                    modifier = Modifier
                        .scale(btnScale)
                        .fillMaxWidth(0.85f),
                    corner = 16.dp
                ) {
                    val outline = Color(0x802D5A0E)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedText(
                            text = if (rewardCoins > 0) {
                                if (isPersian) "تأیید و دریافت ${LocaleHelper.formatNumber(rewardCoins, true)}"
                                else "Onayla ve $rewardCoins Al"
                            } else {
                                if (isPersian) "تأیید" else "Onayla"
                            },
                            textColor = Color.White,
                            outlineColor = outline,
                            outlineWidth = 6f,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            shadowColor = outline.copy(alpha = 0.6f),
                            shadowOffsetY = 3f
                        )
                        if (rewardCoins > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CoinIcon(size = 26.dp)
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (rewardCoins > 0) {
                            if (isPersian) "👆 انتخاب کن و ${LocaleHelper.formatNumber(rewardCoins, true)}"
                            else "👆 Seç ve $rewardCoins"
                        } else {
                            if (isPersian) "👆 یکی رو انتخاب کن" else "👆 Birini seç"
                        },
                        color = DarkBg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = font
                    )
                    if (rewardCoins > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        CoinIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPersian) "هدیه بگیر" else "Hediye Kazan",
                            color = DarkBg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = font
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPersian) "بعداً" else "Sonra",
                color = PopupTextSecondary.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = font,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}