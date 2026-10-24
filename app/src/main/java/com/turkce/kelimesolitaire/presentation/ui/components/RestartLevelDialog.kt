package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayDepth
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

    val coinLabel = if (isPersian) {
        "پرداخت ${LocaleHelper.formatNumber(cost, true)} سکه"
    } else {
        "$cost Altın Öde"
    }
    val adLabel = if (isPersian) "تماشای ویدیو (رایگان)" else "Video İzle (Ücretsiz)"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer dark 3D frame
            Box(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF26005A))
                    .padding(bottom = 7.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF8B35FA),
                                Color(0xFF6414CE),
                                DarkBg,
                                Color(0xFF380084)
                            )
                        )
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {} // block clicks from reaching the scrim
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: title + close button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.restartDialogTitle(isPersian),
                            textColor = Color.White,
                            outlineColor = Color(0xFF190D69),
                            outlineWidth = 12.5f,
                            fontSize = if (isPersian) 26.sp else 21.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 42.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "Close",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(38.dp)
                                .clickable {
                                    GameSettingsManager.playButtonClickSound(context)
                                    onDismiss()
                                }
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Coins balance pill
                            if (isFree) {
                                // ---- حالت رایگان: دو دکمه ----
                                Spacer(modifier = Modifier.height(18.dp))

                                // 1. ریست رایگان (سبز، مثل دکمهٔ خرید فروشگاه)
                                StoreStyleButton(
                                    gradient = listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F)),
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onFreeRestart()
                                    }
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
                                        label = {
                                            Text(
                                                text = LocaleHelper.restartFreeBtn(isPersian),
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = appFont,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. ادامه بازی (آبی)
                                StoreStyleButton(
                                    gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onDismiss()
                                    }
                                ) {
                                    Text(
                                        text = LocaleHelper.continueBtn(isPersian),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = appFont,
                                        textAlign = TextAlign.Center
                                    )
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
                                Spacer(modifier = Modifier.height(18.dp))

                                // Option 1: pay with coins (store green button)
                                StoreStyleButton(
                                    gradient = if (canAfford) {
                                        listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F))
                                    } else {
                                        listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF475569))
                                    },
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onRestartWithCoins()
                                    }
                                ) {
                                    IconLabelRow(
                                        isPersian = isPersian,
                                        icon = { CoinIcon(size = 26.dp) },
                                        label = {
                                            Text(
                                                text = coinLabel,
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = appFont,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Option 2: watch a video (store blue coin-pack button)
                                StoreStyleButton(
                                    gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onRestartWithAd()
                                    }
                                ) {
                                    IconLabelRow(
                                        isPersian = isPersian,
                                        icon = { TvIcon(size = 26.dp) },
                                        label = {
                                            Text(
                                                text = adLabel,
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = appFont,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Same look as the price buttons in StoreScreen (ComboPackCard / CoinPackRow). */
@Composable
private fun StoreStyleButton(
    gradient: List<Color>,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(gradient))
            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** Icon + label; order flipped for Persian, same as the settings menu. */
@Composable
private fun IconLabelRow(
    isPersian: Boolean,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isPersian) {
            label()
            Spacer(modifier = Modifier.width(8.dp))
            icon()
        } else {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            label()
        }
    }
}