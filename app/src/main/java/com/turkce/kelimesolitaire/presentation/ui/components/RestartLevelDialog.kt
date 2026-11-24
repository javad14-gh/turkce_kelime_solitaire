package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.buildSpannedString
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont
import kotlinx.coroutines.delay

@Composable
fun RestartLevelDialog(
    coins: Int,
    cost: Int = 15,
    isFirstFree: Boolean = false,
    onFreeRestart: () -> Unit = {},
    onRestartWithCoins: () -> Unit,
    onRestartWithAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val appFont = rememberAppFont()
    var remainingMs by remember {
        mutableStateOf(
            GameSettingsManager.getFreeRestartRemainingMs(
                context
            )
        )
    }
    LaunchedEffect(Unit) {
        while (remainingMs > 0) {
            delay(1000)
            remainingMs = GameSettingsManager.getFreeRestartRemainingMs(context)
        }
    }
    val isFree = isFirstFree || remainingMs <= 0

    PopupFrame(
        title = LocaleHelper.restartDialogTitle(isPersian),
        //onDismiss = onDismiss,
        titleSize = if (isPersian) 26.sp else 21.sp
    ) {
        if(!isFree){
            Text(
                text = LocaleHelper.restartNextFreeIn(formatMmSs(remainingMs), isPersian),
                color = MenuRedBack,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = appFont
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = when {
                isFirstFree -> LocaleHelper.restartFreePrompt(isPersian)
                isFree -> LocaleHelper.restartHourlyPrompt(isPersian)
                else -> LocaleHelper.restartDialogPrompt(isPersian)
            },
            color = PopupTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = appFont,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (isFree) {
            MenuWideButton(
                text = LocaleHelper.restartFreeBtn(isPersian),
                isPersian = isPersian,
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onFreeRestart()
                },
                icon = { MenuIcon(R.drawable.restart, "restart") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuWideButton(
                text = LocaleHelper.continueBtn(isPersian),
                isPersian = isPersian,
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onDismiss()
                },
                icon = { MenuIcon(R.drawable.undo, "return") }
            )

        } else {

            val coinLabel = if (isPersian) "پرداخت ${LocaleHelper.formatNumber(cost, true)} سکه"
            else "$cost Altın Öde"
            Spacer(modifier = Modifier.height(10.dp))
            MenuWideButton(
                text = coinLabel,
                isPersian = isPersian,
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onRestartWithCoins()   // اگر سکه کم باشد ViewModel خودش پیام می‌دهد
                },
                icon = {MenuIcon(R.drawable.coin,"coin")}
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuWideButton(
                text = if (isPersian) "تماشای ویدیو" else "Video İzle",
                isPersian = isPersian,
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onRestartWithAd()
                },
                icon = {MenuIcon(R.drawable.tv,"tv")}
            )
            Spacer(modifier = Modifier.height(12.dp))
            MenuWideButton(
                text = LocaleHelper.continueBtn(isPersian),
                isPersian = isPersian,
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onDismiss()
                },
                icon = { MenuIcon(R.drawable.undo, "return") }
            )
        }
    }
}

private fun formatMmSs(ms: Long): String {
    val total = ((ms + 999) / 1000).toInt()
    return String.format(java.util.Locale.US, "%02d:%02d", total / 60, total % 60)
}