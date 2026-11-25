package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain
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
    if (tutorialType == TutorialType.LEVEL_1_GUIDE ||
        tutorialType == TutorialType.LEVEL_2_GUIDE) return

    val context = LocalContext.current

    val title = when (tutorialType) {
        TutorialType.UNDO_UNLOCK ->
            if (isPersian) "قابلیت بازگشت باز شد!" else "Geri Al Açıldı!"
        TutorialType.HINT_UNLOCK ->
            if (isPersian) "راهنما باز شد!" else "İpucu Açıldı!"
        else ->
            if (isPersian) "کارت جوکر باز شد!" else "Joker Açıldı!"
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
    else "🎁 $giftCount Ücretsiz Kullanım"

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

    PopupFrame(
        title = title,
        onDismiss = onDismiss,
        titleSize = if (isPersian) 24.sp else 22.sp
    ) {
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(84.dp),
            contentAlignment = Alignment.Center
        ) {
            when (tutorialType) {
                TutorialType.UNDO_UNLOCK -> UndoIcon(size = 84.dp)
                TutorialType.HINT_UNLOCK -> HintIcon(size = 84.dp)
                else -> JokerIcon(size = 84.dp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = description,
            color = DarkMain,
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
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .heightIn(min = 60.dp),
            corner = 16.dp,
            elevation = 10.dp
        ) {
            OutlinedText(
                text = giftText,
                textColor = Color.White,
                outlineColor = DarkMain,
                outlineWidth = 6f,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                shadowColor = DarkMain.copy(alpha = 0.6f),
                shadowOffsetY = 3f
            )

        }
    }
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFF303030,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun BoosterUnlockDialogPreview() {
    BoosterUnlockDialog(
        onDismiss = {},
        tutorialType = TutorialType.UNDO_UNLOCK,
        isPersian = true,
        fontFamily = rememberNunitoFont()
    )
}