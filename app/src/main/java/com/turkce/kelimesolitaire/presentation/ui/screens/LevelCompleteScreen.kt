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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary

@Composable
fun LevelCompleteScreen(
    levelNumber: Int,
    bonusCoins: Int,
    onNextLevelClicked: () -> Unit,
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

            // Victory Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏆 HARİKA!",
                    color = SuccessGreen,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Seviye $levelNumber Tamamlandı",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reward Summary Card
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .background(DarkCard, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KAZANILAN ÖDÜL",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 28.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+$bonusCoins Altın",
                            color = AccentGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Navigation Actions with 3D Tactile Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 3D Next Level Button (Lime Green Gradient)
                Box(
                    modifier = Modifier
                        .shadow(14.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                            )
                        )
                        .padding(4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1E3A07))
                        .padding(bottom = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFA3E635),
                                    Color(0xFF65A30D),
                                    Color(0xFF4D7C0F)
                                )
                            )
                        )
                        .clickable { onNextLevelClicked() }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SONRAKİ SEVİYE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Main Menu Button (Slate Glass)
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
                                colors = listOf(
                                    Color(0xFF334155),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .clickable { onMainMenuClicked() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ANA MENÜ",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Ad Banner Footer
            AdBannerPlaceholder()
        }
    }
}
