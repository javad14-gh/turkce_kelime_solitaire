package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
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

            // Central option card
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

                    // Continue with coins if enough
                    Button(
                        onClick = onContinueForCoins,
                        enabled = coins >= 50,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(45.dp)
                            .background(
                                if (coins >= 50) Brush.horizontalGradient(listOf(AccentGold, SecondaryNeon))
                                else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            text = "🪙 50 Altın Harca",
                            color = if (coins >= 50) Color.Black else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Continue with rewarded ad
                    Button(
                        onClick = onContinueForAd,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(45.dp)
                            .background(
                                Brush.horizontalGradient(listOf(SecondaryNeon, PrimaryNeon)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            text = "📺 Ücretsiz (+15 Hamle)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Control Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restart level
                Button(
                    onClick = onRestartClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .height(45.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFE57373), ErrorRed)),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = "TEKRAR DENE",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Return to Main Menu
                Button(
                    onClick = onMainMenuClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(220.dp)
                        .height(45.dp)
                        .background(
                            Brush.horizontalGradient(listOf(BorderGlass, DarkCard)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "ANA MENÜ",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Static Bottom Ad
            AdBannerPlaceholder()
        }
    }
}
