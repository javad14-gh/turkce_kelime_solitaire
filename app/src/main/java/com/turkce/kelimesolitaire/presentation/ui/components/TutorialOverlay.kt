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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameBase
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameGradient
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayDepth
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import androidx.compose.ui.platform.LocalContext
import com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType

/**
 * Popup shown when a booster (undo / hint / joker) is unlocked.
 * Same visual structure as the other popups: dark frame, header, light tray.
 */
@Composable
fun BoosterUnlockDialog(
    tutorialType: TutorialType,
    isPersian: Boolean,
    fontFamily: FontFamily,
    giftCount: Int = 1,
    onDismiss: () -> Unit
) {
    // Only booster tutorials are handled here
    if (tutorialType == TutorialType.LEVEL_1_GUIDE ||
        tutorialType == TutorialType.LEVEL_2_GUIDE) return

    val context = LocalContext.current

    val title = when (tutorialType) {
        TutorialType.UNDO_UNLOCK ->
            if (isPersian) "قابلیت بازگشت باز شد! 🎉" else "Geri Al Açıldı! 🎉"
        TutorialType.HINT_UNLOCK ->
            if (isPersian) "راهنما باز شد! 💡" else "İpucu Açıldı! 💡"
        else ->
            if (isPersian) "کارت جوکر باز شد! 🃏" else "Joker Açıldı! 🃏"
    }

    val description = when (tutorialType) {
        TutorialType.UNDO_UNLOCK ->
            if (isPersian) "حرکت اشتباه زدی؟ دکمه‌ی بازگشت آخرین حرکتت را برمی‌گرداند."
            else "Yanlış hamle mi yaptın? Geri Al düğmesi son hamleni geri alır."
        TutorialType.HINT_UNLOCK ->
            if (isPersian) "گیر کردی؟ دکمه‌ی راهنما بهترین حرکت ممکن را نشانت می‌دهد."
            else "Takıldın mı? İpucu düğmesi sana en iyi hamleyi gösterir."
        else ->
            if (isPersian) "جوکر با هر دسته‌ای جور می‌شود و راه بسته را باز می‌کند."
            else "Joker her kategoriyle eşleşir ve tıkanan yolu açar."
    }

    val giftText = if (isPersian)
        "🎁 ${LocaleHelper.formatNumber(giftCount, true)} بار استفاده‌ی رایگان"
    else
        "🎁 $giftCount Ücretsiz Kullanım"

    val buttonText = if (isPersian) "متوجه شدم" else "Anladım"

    // Back button closes this dialog instead of opening the (hidden) exit dialog
    BackHandler { onDismiss() }

    val pulse = rememberInfiniteTransition(label = "boosterPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boosterPulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block outside clicks */ },
        contentAlignment = Alignment.Center
    ) {
        // Outer dark 3D frame
        Box(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(PopupFrameBase)
                .padding(bottom = 7.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(PopupFrameGradient))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = title,
                        textColor = Color.White,
                        outlineColor = PopupTitleOutline,
                        outlineWidth = 12.5f,
                        fontSize = if (isPersian) 24.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // Inner light tray
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                        .shadow(10.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(PopupTrayDepth)
                        .padding(bottom = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PopupBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing booster icon
                        Box(
                            modifier = Modifier
                                .scale(pulseScale)
                                .size(84.dp)
                                .shadow(10.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFE066), AccentGold, Color(0xFFD97706))
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when (tutorialType) {
                                TutorialType.UNDO_UNLOCK -> UndoIcon(size = 50.dp)
                                TutorialType.HINT_UNLOCK -> HintIcon(size = 50.dp)
                                else -> JokerIcon(size = 50.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Free gift pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PopupButtonBg)
                                .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = giftText,
                                color = Color(0xFF15803D),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = fontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = description,
                            color = PopupTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        CandyButton(
                            onClick = {
                                GameSettingsManager.playButtonClickSound(context)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            corner = 16.dp
                        ) {
                            val outline = Color(0x802D5A0E)
                            OutlinedText(
                                text = buttonText,
                                textColor = Color.White,
                                outlineColor = outline,
                                outlineWidth = 6f,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                shadowColor = outline.copy(alpha = 0.6f),
                                shadowOffsetY = 3f
                            )
                        }
                    }
                }
            }
        }
    }
}