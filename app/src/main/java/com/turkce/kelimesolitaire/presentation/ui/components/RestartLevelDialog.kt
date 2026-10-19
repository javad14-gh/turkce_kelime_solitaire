package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont

@Composable
fun RestartLevelDialog(
    coins: Int,
    cost: Int = 15,
    onRestartWithCoins: () -> Unit,
    onRestartWithAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPersian = LocaleHelper.isPersian(context)
    val appFont = rememberAppFont()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg)
                    .border(2.dp, com.turkce.kelimesolitaire.presentation.ui.theme.PopupBorder, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header Icon
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.restart),
                        contentDescription = "Restart Level",
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog Title
                    Text(
                        text = LocaleHelper.restartDialogTitle(isPersian),
                        color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = appFont,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prompt message
                    Text(
                        text = LocaleHelper.restartDialogPrompt(isPersian),
                        color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = appFont,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Coins Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg)
                            .border(1.dp, com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonShadow, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        CoinIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${LocaleHelper.formatNumber(coins, isPersian)} ${if (isPersian) "سکه موجود" else "Mevcut Altın"}",
                            color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = appFont
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option 1: Pay 15 Coins (3D Button with PopupButtonBg)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonShadow)
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg)
                            .border(1.dp, com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                GameSettingsManager.playButtonClickSound(context)
                                onRestartWithCoins()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LocaleHelper.restartWithCoinsBtn(cost, isPersian),
                            color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = appFont
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Watch Ad (3D Button with PopupButtonBg)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonShadow)
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg)
                            .border(1.dp, com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                GameSettingsManager.playButtonClickSound(context)
                                onRestartWithAd()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.tv),
                                contentDescription = "Watch Ad",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LocaleHelper.restartWithAdBtn(isPersian),
                                color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = appFont
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Option 3: Continue Playing / Dismiss
                    Text(
                        text = LocaleHelper.continuePlayingBtn(isPersian),
                        color = com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = appFont,
                        modifier = Modifier
                            .clickable {
                                GameSettingsManager.playButtonClickSound(context)
                                onDismiss()
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
