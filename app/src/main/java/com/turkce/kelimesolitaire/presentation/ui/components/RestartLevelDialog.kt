package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont

@Composable
fun RestartLevelDialog(
    coins: Int,
    cost: Int = 15,
    isFree: Boolean = false,
    onFreeRestart: () -> Unit = {},
    onRestartWithCoins: () -> Unit,
    onRestartWithAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val appFont = rememberAppFont()
    val canAfford = coins >= cost

    val green = listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F))
    val greenOutline = Color(0x802D5A0E)
    val blue = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
    val blueOutline = Color(0xFF0284C7).darker(0.35f)
    val grey = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF475569))
    val greyOutline = Color(0xFF475569).darker(0.35f)

    PopupFrame(
        title = LocaleHelper.restartDialogTitle(isPersian),
        onDismiss = onDismiss,
        titleSize = if (isPersian) 26.sp else 21.sp
    ) {
        Image(
            painter = painterResource(id = R.drawable.restart),
            contentDescription = "Restart Level",
            modifier = Modifier.size(52.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isFree) LocaleHelper.restartFreePrompt(isPersian)
            else LocaleHelper.restartDialogPrompt(isPersian),
            color = PopupTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = appFont,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (isFree) {
            CandyButton(
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onFreeRestart()
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = green,
                outlineColor = greenOutline
            ) {
                IconLabelRow(
                    isPersian = isPersian,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.restart),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { PopupButtonLabel(LocaleHelper.restartFreeBtn(isPersian), greenOutline) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CandyButton(
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = blue,
                outlineColor = blueOutline
            ) {
                PopupButtonLabel(LocaleHelper.continueBtn(isPersian), blueOutline)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PopupButtonBg)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                CoinIcon(size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${LocaleHelper.formatNumber(coins, isPersian)} " +
                            if (isPersian) "سکه موجود" else "Mevcut Altın",
                    color = PopupTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = appFont
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val coinLabel = if (isPersian) "پرداخت ${LocaleHelper.formatNumber(cost, true)} سکه"
            else "$cost Altın Öde"
            CandyButton(
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onRestartWithCoins()   // اگر سکه کم باشد ViewModel خودش پیام می‌دهد
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = if (canAfford) green else grey,
                outlineColor = if (canAfford) greenOutline else greyOutline
            ) {
                IconLabelRow(
                    isPersian = isPersian,
                    icon = { CoinIcon(size = 26.dp) },
                    label = { PopupButtonLabel(coinLabel, if (canAfford) greenOutline else greyOutline) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            CandyButton(
                onClick = {
                    GameSettingsManager.playButtonClickSound(context)
                    onRestartWithAd()
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = blue,
                outlineColor = blueOutline
            ) {
                IconLabelRow(
                    isPersian = isPersian,
                    icon = { TvIcon(size = 26.dp) },
                    label = {
                        PopupButtonLabel(
                            if (isPersian) "تماشای ویدیو (رایگان)" else "Video İzle (Ücretsiz)",
                            blueOutline
                        )
                    }
                )
            }
        }
    }
}