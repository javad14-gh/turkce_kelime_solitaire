package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.ErrorRed
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    levelNumber: Int,
    coins: Int,
    onContinueForCoins: () -> Unit,
    onContinueForAd: () -> Unit,
    onRestartClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Defeat Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HAMLELER BİTTİ!",
                    color = ErrorRed,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seviye $levelNumber'de hamleleriniz tükendi.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Central Option Card
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .background(DarkCard, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Oyuna Devam Et (+15 Hamle)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3D Continue for Coins Button
                    Box(
                        modifier = Modifier
                            .shadow(10.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(3.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (coins >= 50) Color(0xFFB8860B) else Color(0xFF475569))
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (coins >= 50) {
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF64748B), Color(0xFF475569))
                                    )
                                }
                            )
                            .clickable(enabled = coins >= 50) { onContinueForCoins() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 20.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "50 Altın Harca",
                                color = if (coins >= 50) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3D Continue for Ad Button
                    Box(
                        modifier = Modifier
                            .shadow(10.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(3.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFF0369A1))
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                                )
                            )
                            .clickable { onContinueForAd() }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.turkce.kelimesolitaire.presentation.ui.components.AdIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ücretsiz (+15 Hamle)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            )
                        }
                    }
                }
            }

            // Control Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3D Restart Level Button (Red)
                Box(
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                            )
                        )
                        .padding(3.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0xFF991B1B))
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFAF2626), Color(0xFFDC2626), Color(0xFF991B1B))
                            )
                        )
                        .clickable { onRestartClicked() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TEKRAR DENE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                }

                // 3D Main Menu Button (Slate)
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF64748B), Color(0xFF334155))
                            )
                        )
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(bottom = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                        )
                        .clickable { onMainMenuClicked() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ANA MENÜ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Static Bottom Ad
            AdBannerPlaceholder()
        }
    }
}
