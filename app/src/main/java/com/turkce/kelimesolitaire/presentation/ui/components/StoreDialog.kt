package com.turkce.kelimesolitaire.presentation.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.zIndex

@Composable
fun StoreDialog(
    isAdFree: Boolean,
    onClose: () -> Unit,
    onWatchAdForCoins: () -> Unit,
    onBuyCoinPack: (Int) -> Unit,
    onBuyRemoveAds: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .zIndex(300f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .shadow(24.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF3826B4), Color(0xFF241584), Color(0xFF190D69))
                    )
                )
                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(26.dp))
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Gold Arched Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFB8860B), Color(0xFF78350F))
                            )
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = "🛒 MAĞAZA & ÖDÜLLER",
                        textColor = Color.White,
                        outlineColor = Color(0xFF78350F),
                        outlineWidth = 5f,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    // Close (X) Button Top Right
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B))
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Store Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. FREE COINS VIA REWARDED AD CARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E1B4B))
                            .border(1.5.dp, Color(0xFF818CF8), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📺 Ücretsiz Altın",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                                Text(
                                    text = "Kısa bir reklam izle ve 50 Altın kazan!",
                                    color = Color(0xFFC7D2FE),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .shadow(6.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F))
                                        )
                                    )
                                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onClose()
                                        onWatchAdForCoins()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AdIcon(size = 16.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+50",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    CoinIcon(size = 14.dp)
                                }
                            }
                        }
                    }

                    // 2. COIN PACKAGES SECTION
                    Text(
                        text = "💰 ALTIN PAKETLERİ",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                    )

                    // Coin Pack 100
                    CoinPackRow(
                        coinAmount = 100,
                        priceText = "₺19.99",
                        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        onBuy = { onBuyCoinPack(100) }
                    )

                    // Coin Pack 500
                    CoinPackRow(
                        coinAmount = 500,
                        priceText = "₺49.99",
                        gradientColors = listOf(Color(0xFFC084FC), Color(0xFF9333EA)),
                        badgeText = "ÇOK POPÜLER",
                        onBuy = { onBuyCoinPack(500) }
                    )

                    // Coin Pack 1500
                    CoinPackRow(
                        coinAmount = 1500,
                        priceText = "₺99.99",
                        gradientColors = listOf(Color(0xFFFFD700), Color(0xFFD97706)),
                        badgeText = "EN İYİ FİYAT",
                        onBuy = { onBuyCoinPack(1500) }
                    )

                    // 3. REMOVE ADS PERMANENT UPGRADE CARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isAdFree) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF065F46), Color(0xFF047857))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF831843), Color(0xFF9D174D), Color(0xFF701A75))
                                    )
                                }
                            )
                            .border(1.5.dp, if (isAdFree) Color(0xFF34D399) else Color(0xFFF472B6), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚫 Reklamları Kaldır",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                                Text(
                                    text = if (isAdFree) "Tüm reklamlar kaldırıldı!" else "Tüm geçiş ve banner reklamlarını sonsuza dek kapat!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            if (isAdFree) {
                                Text(
                                    text = "✅ AKTİF",
                                    color = Color(0xFF34D399),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .shadow(6.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFF9F1239))
                                            )
                                        )
                                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                        .clickable { onBuyRemoveAds() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₺39.99",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
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

@Composable
private fun CoinPackRow(
    coinAmount: Int,
    priceText: String,
    gradientColors: List<Color>,
    badgeText: String? = null,
    onBuy: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinIcon(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$coinAmount Altın",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(gradientColors))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .clickable { onBuy() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priceText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }
        }
    }
}
