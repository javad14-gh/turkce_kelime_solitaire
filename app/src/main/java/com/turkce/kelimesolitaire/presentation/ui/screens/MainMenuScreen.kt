package com.turkce.kelimesolitaire.presentation.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    levelNumber: Int,
    coins: Int,
    completedLevels: Set<Int>,
    onStartGameClicked: (Int) -> Unit,
    onWatchAdForCoins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calculate last unsolved level dynamically
    val lastUnsolvedLevel = remember(completedLevels) {
        var lvl = 1
        while (completedLevels.contains(lvl)) {
            lvl++
        }
        lvl
    }

    // Determine difficulty of the last unsolved level
    val difficulty = remember(lastUnsolvedLevel) {
        val cycleIndex = (lastUnsolvedLevel - 1) % 10
        when (cycleIndex) {
            0, 1, 3, 7 -> "Kolay"
            2, 4, 5, 8 -> "Orta"
            6 -> "Zor"
            else -> "CokZor"
        }
    }

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
            
            // Top Dashboard HUD (Coins Status Pill matching reference design)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A364F).copy(alpha = 0.9f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .padding(start = 4.dp, end = 16.dp, top = 3.dp, bottom = 3.dp)
                ) {
                    // Gold Coin Icon + Green '+' Circle Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 30.dp)
                        Box(
                            modifier = Modifier
                                .offset(x = 3.dp, y = 3.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$coins",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Central Game Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TÜRKÇE KELİME",
                    color = SecondaryNeon,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "EŞLEŞTİRME SOLİTAİRE",
                    color = PrimaryNeon,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Kelimeleri sürükle, grupları eşleştir, bölümleri tamamla!",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // 3D Juicy Play Button with Difficulty Ribbon Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
                ) {
                    // Compact 3D Green Button Shell (10% wider than text with 3D bottom bevel)
                    Box(
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            ) // Cream/white 3D outer rim shell
                            .padding(4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E3A07)) // Dark 3D bottom base shadow
                            .padding(bottom = 5.dp) // Creates thick 3D bottom bevel
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFA3E635), // Glossy top highlight green
                                        Color(0xFF65A30D), // Mid vibrant green
                                        Color(0xFF4D7C0F)  // Inner shade
                                    )
                                )
                            )
                            .clickable { onStartGameClicked(lastUnsolvedLevel) }
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SEVİYE $lastUnsolvedLevel",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Overlapping Difficulty Ribbon Banner (Shown ONLY for Zor and CokZor levels, enlarged size)
                    if (difficulty == "Zor" || difficulty == "CokZor") {
                        val difficultyText = if (difficulty == "CokZor") "Süper Zor" else "Zor"
                        val ribbonColor = if (difficulty == "CokZor") Color(0xFFDC2626) else Color(0xFFEA580C)

                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(ribbonColor)
                                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 22.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = difficultyText,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3D Watch Ad Button
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
                        .background(Color(0xFFB8860B))
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFF59E0B),
                                    Color(0xFFD97706)
                                )
                            )
                        )
                        .clickable { onWatchAdForCoins() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.turkce.kelimesolitaire.presentation.ui.components.AdIcon(size = 20.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reklam İzle (+50 ",
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 18.dp)
                        Text(
                            text = ")",
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                    }
                }
            }

            // Footer Section: Ad Banner and Policy Hooks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdBannerPlaceholder()
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gizlilik Politikası",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.privacy_policy_url)))
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                    Text(
                        text = "•",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "Veri Güvenliği",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.data_safety_url)))
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
