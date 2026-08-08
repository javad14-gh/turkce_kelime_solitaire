package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold

@Composable
fun StoreScreen(
    coins: Int,
    isAdFree: Boolean,
    onClose: () -> Unit,
    onWatchAdForCoins: () -> Unit,
    onBuyCoinPack: (Int) -> Unit,
    onBuyRemoveAds: () -> Unit,
    onBuyBundle: (coinsAmount: Int, removeAds: Boolean) -> Unit = { amount, removeAds ->
        onBuyCoinPack(amount)
        if (removeAds) onBuyRemoveAds()
    }
) {
    BackHandler { onClose() }

    val nunitoFont = rememberNunitoFont()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B0E4D), Color(0xFF0F0738), Color(0xFF07031D))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // 1. HEADER BAR (Coins Left, Cancel Right)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF59E0B), Color(0xFFB8860B), Color(0xFF78350F))
                        )
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // LEFT: Coins Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    CoinIcon(size = 24.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$coins",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }

                // RIGHT: Cancel / Close Button
                Image(
                    painter = painterResource(id = R.drawable.cancel),
                    contentDescription = "Close Store",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 2. SCROLLABLE BANNERS LIST
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ------------------------------------------
                // BANNER 1: REMOVE ADS (HUGE ICON TO EDGES)
                // ------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isAdFree) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFF9D174D))
                                )
                            }
                        )
                        .border(
                            2.dp,
                            if (isAdFree) Color(0xFF34D399) else Color(0xFFF472B6),
                            RoundedCornerShape(22.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_ads),
                            contentDescription = "No Ads",
                            modifier = Modifier
                                .size(110.dp)
                                .offset(x = (-6).dp)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "REKLAMLARI KALDIR",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAdFree) "Tüm reklamlar kaldırıldı!" else "Geçiş ve banner reklamlarını sonsuza dek kapat!",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isAdFree) {
                                        Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
                                    }
                                )
                                .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                                .clickable(enabled = !isAdFree) { onBuyRemoveAds() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAdFree) "AKTİF" else "₺49.99",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                }

                // ------------------------------------------
                // BANNER 2: MEGA PAKET
                // ------------------------------------------
                ComboPackCard(
                    title = "MEGA PAKET",
                    badge = "EFSANE FIRSAT",
                    badgeColor = Color(0xFFDC2626),
                    price = "₺149.99",
                    gradientColors = listOf(Color(0xFF581C87), Color(0xFF3B0764), Color(0xFF2E1065)),
                    borderColor = Color(0xFFFFD700),
                    isGoldPriceButton = true,
                    row1Items = listOf(
                        R.drawable.coins to "2000",
                        R.drawable.no_ads to "Reklam Yok"
                    ),
                    row2Items = listOf(
                        R.drawable.hint to "3x",
                        R.drawable.undo to "3x",
                        R.drawable.joker to "3x"
                    ),
                    onBuy = { onBuyBundle(2900, true) }
                )

                // ------------------------------------------
                // COMBO 1: SÜPER PAKET
                // ------------------------------------------
                ComboPackCard(
                    title = "SÜPER PAKET",
                    badge = "ÇOK SATAN",
                    badgeColor = Color(0xFFDC2626),
                    price = "₺89.99",
                    gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF)),
                    borderColor = Color(0xFF60A5FA),
                    row1Items = listOf(
                        R.drawable.coins to "1000",
                        R.drawable.hint to "2x"
                    ),
                    row2Items = listOf(
                        R.drawable.undo to "2x",
                        R.drawable.joker to "2x"
                    ),
                    onBuy = { onBuyBundle(1600, false) }
                )

                // ------------------------------------------
                // COMBO 2: AVANTAJ PAKETİ
                // ------------------------------------------
                ComboPackCard(
                    title = "AVANTAJ PAKETİ",
                    badge = "FIRSAT",
                    badgeColor = Color(0xFFDC2626),
                    price = "₺49.99",
                    gradientColors = listOf(Color(0xFF065F46), Color(0xFF047857)),
                    borderColor = Color(0xFF34D399),
                    row1Items = listOf(
                        R.drawable.coins to "500",
                        R.drawable.hint to "1x"
                    ),
                    row2Items = listOf(
                        R.drawable.undo to "1x",
                        R.drawable.joker to "1x"
                    ),
                    onBuy = { onBuyBundle(800, false) }
                )

                // ------------------------------------------
                // COIN BANNERS SECTION HEADER (CUSTOM COIN ICON)
                // ------------------------------------------
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CoinIcon(size = 28.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALTIN PAKETLERİ",
                        color = AccentGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }

                // ------------------------------------------
                // 1st Coin Banner: FREE COINS (PLAY ICON INSIDE BUTTON)
                // ------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF3B82F6))
                            )
                        )
                        .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.tv),
                                contentDescription = "TV Ad",
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ücretsiz Altın",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                                Text(
                                    text = "Reklam izle & 50 Altın kazan",
                                    color = Color(0xFFDBEAFE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

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
                                .clickable { onWatchAdForCoins() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.play),
                                    contentDescription = "Watch Ad",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+50",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------
                // PAID COIN BANNERS WITH TOP-CENTER OVERLAPPING RED BADGES
                // ------------------------------------------
                CoinPackRow(
                    amountText = "500",
                    priceText = "₺39.99",
                    gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    onBuy = { onBuyCoinPack(500) }
                )

                CoinPackRow(
                    amountText = "1000",
                    priceText = "₺69.99",
                    gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                    badgeText = "POPÜLER",
                    badgeColor = Color(0xFFDC2626),
                    onBuy = { onBuyCoinPack(1000) }
                )

                CoinPackRow(
                    amountText = "2500",
                    priceText = "₺129.99",
                    gradientColors = listOf(Color(0xFFFFD700), Color(0xFFD97706)),
                    badgeText = "EN İYİ FİYAT",
                    badgeColor = Color(0xFFDC2626),
                    onBuy = { onBuyCoinPack(2500) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Ad Banner Placeholder if not ad-free
            if (!isAdFree) {
                AdBannerPlaceholder()
            }
        }
    }
}

@Composable
private fun BundleItemChip(iconRes: Int, text: String) {
    val nunitoFont = rememberNunitoFont()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = nunitoFont
        )
    }
}

@Composable
private fun ComboPackCard(
    title: String,
    badge: String? = null,
    badgeColor: Color = Color(0xFFDC2626),
    price: String,
    gradientColors: List<Color>,
    borderColor: Color,
    isGoldPriceButton: Boolean = false,
    row1Items: List<Pair<Int, String>>,
    row2Items: List<Pair<Int, String>>,
    onBuy: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(top = if (badge != null) 6.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .shadow(10.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(gradientColors))
                .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT 2/3 PORTION: 2 Lines of Items Spaced Evenly
                Column(
                    modifier = Modifier
                        .weight(2.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Items Line 1 (Spaced Evenly across full width)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row1Items.forEach { (iconRes, text) ->
                            BundleItemChip(iconRes = iconRes, text = text)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Items Line 2 (Spaced Evenly across full width)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row2Items.forEach { (iconRes, text) ->
                            BundleItemChip(iconRes = iconRes, text = text)
                        }
                    }
                }

                // Vertical Separator Line
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.85f)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // RIGHT 1/3 PORTION: Package Title ABOVE Price Button (All Centered)
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (isGoldPriceButton) AccentGold else Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isGoldPriceButton) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F))
                                    )
                                }
                            )
                            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                            .clickable { onBuy() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = price,
                            color = if (isGoldPriceButton) Color(0xFF0F172A) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }
                }
            }
        }

        // TOP CENTER OVERLAPPING RED BADGE
        if (badge != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .shadow(6.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor)
                    .border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }
        }
    }
}

@Composable
private fun CoinPackRow(
    amountText: String,
    priceText: String,
    gradientColors: List<Color>,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFDC2626),
    onBuy: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(top = if (badgeText != null) 6.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.coins),
                        contentDescription = "Coins Stack",
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = amountText,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(gradientColors))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .clickable { onBuy() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priceText,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }
            }
        }

        // TOP CENTER OVERLAPPING RED BADGE
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .shadow(6.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor)
                    .border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }
        }
    }
}
