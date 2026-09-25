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
import androidx.compose.foundation.layout.defaultMinSize
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
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig

@Composable
fun StoreScreen(
    coins: Int,
    isAdFree: Boolean,
    isStarterPackPurchased: Boolean = false,
    onClose: () -> Unit,
    onWatchAdForCoins: () -> Unit,
    onPurchaseSku: (String) -> Unit = {},
    onRestorePurchases: () -> Unit = {}
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
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
                        text = LocaleHelper.formatNumber(coins, isPersian),
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
                if (!isAdFree) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFF9D174D))
                                )
                            )
                            .border(
                                2.dp,
                                Color(0xFFF472B6),
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
                                    text = LocaleHelper.removeAdsTitle(isPersian),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocaleHelper.removeAdsDesc(isPersian),
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
                                        Brush.verticalGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
                                    )
                                    .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                                    .clickable { onPurchaseSku(MyketBillingConfig.SKU_REMOVE_ADS) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isPersian) "۳۹,۰۰۰ تومان" else "₺49.99",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------
                // BANNER 2: STARTER PACK (بسته شروع / BAŞLANGIÇ PAKETİ - ONE TIME OFFER)
                // ------------------------------------------
                if (!isStarterPackPurchased) {
                    ComboPackCard(
                        title = if (isPersian) "بسته شروع" else "BAŞLANGIÇ PAKETİ",
                        badge = if (isPersian) "پیشنهاد ویژه" else "ÖZEL FIRSAT",
                        badgeColor = Color(0xFFDC2626),
                        price = if (isPersian) "۹۹,۰۰۰ تومان" else "₺99.99",
                        gradientColors = listOf(Color(0xFF581C87), Color(0xFF3B0764), Color(0xFF2E1065)),
                        borderColor = Color(0xFFFFD700),
                        isGoldPriceButton = true,
                        row1Items = if (isPersian) listOf(
                            R.drawable.coins to "۲۰۰۰",
                            R.drawable.no_ads to "بدون تبلیغ"
                        ) else listOf(
                            R.drawable.coins to "2000",
                            R.drawable.no_ads to "Reklam Yok"
                        ),
                        row2Items = if (isPersian) listOf(
                            R.drawable.hint to "۳×",
                            R.drawable.undo to "۳×",
                            R.drawable.joker to "۳×"
                        ) else listOf(
                            R.drawable.hint to "3x",
                            R.drawable.undo to "3x",
                            R.drawable.joker to "3x"
                        ),
                        onBuy = { onPurchaseSku(MyketBillingConfig.SKU_STARTER_PACK) }
                    )
                }

                // ------------------------------------------
                // BANNER 3: NEW MEGA PAKET (CONSUMABLE, 4000 COINS + 4x BOOSTERS)
                // ------------------------------------------
                ComboPackCard(
                    title = if (isPersian) "بسته مگا" else "MEGA PAKET",
                    badge = if (isPersian) "پرقدرت‌ترین" else "EFSANE FIRSAT",
                    badgeColor = Color(0xFFD97706),
                    price = if (isPersian) "۱۴۹,۰۰۰ تومان" else "₺149.99",
                    gradientColors = listOf(Color(0xFF701A75), Color(0xFF4C0519), Color(0xFF2E0213)),
                    borderColor = Color(0xFFF472B6),
                    isGoldPriceButton = true,
                    row1Items = if (isPersian) listOf(
                        R.drawable.coins to "۴۰۰۰",
                        R.drawable.hint to "۴×"
                    ) else listOf(
                        R.drawable.coins to "4000",
                        R.drawable.hint to "4x"
                    ),
                    row2Items = if (isPersian) listOf(
                        R.drawable.undo to "۴×",
                        R.drawable.joker to "۴×"
                    ) else listOf(
                        R.drawable.undo to "4x",
                        R.drawable.joker to "4x"
                    ),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_MEGA) }
                )

                // ------------------------------------------
                // COMBO 1: SÜPER PAKET (بسته ویژه)
                // ------------------------------------------
                ComboPackCard(
                    title = if (isPersian) "بسته ویژه" else "SÜPER PAKET",
                    badge = if (isPersian) "پرفروش" else "ÇOK SATAN",
                    badgeColor = Color(0xFFDC2626),
                    price = if (isPersian) "۸۹,۰۰۰ تومان" else "₺89.99",
                    gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF)),
                    borderColor = Color(0xFF60A5FA),
                    row1Items = if (isPersian) listOf(
                        R.drawable.coins to "۱۰۰۰",
                        R.drawable.hint to "۲×"
                    ) else listOf(
                        R.drawable.coins to "1000",
                        R.drawable.hint to "2x"
                    ),
                    row2Items = if (isPersian) listOf(
                        R.drawable.undo to "۲×",
                        R.drawable.joker to "۲×"
                    ) else listOf(
                        R.drawable.undo to "2x",
                        R.drawable.joker to "2x"
                    ),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_SPECIAL) }
                )

                // ------------------------------------------
                // COMBO 2: AVANTAJ PAKETİ (بسته اقتصادی)
                // ------------------------------------------
                ComboPackCard(
                    title = if (isPersian) "بسته اقتصادی" else "AVANTAJ PAKETİ",
                    badge = if (isPersian) "به‌صرفه" else "FIRSAT",
                    badgeColor = Color(0xFFDC2626),
                    price = if (isPersian) "۴۹,۰۰۰ تومان" else "₺49.99",
                    gradientColors = listOf(Color(0xFF065F46), Color(0xFF047857)),
                    borderColor = Color(0xFF34D399),
                    row1Items = if (isPersian) listOf(
                        R.drawable.coins to "۵۰۰",
                        R.drawable.hint to "۱×"
                    ) else listOf(
                        R.drawable.coins to "500",
                        R.drawable.hint to "1x"
                    ),
                    row2Items = if (isPersian) listOf(
                        R.drawable.undo to "۱×",
                        R.drawable.joker to "۱×"
                    ) else listOf(
                        R.drawable.undo to "1x",
                        R.drawable.joker to "1x"
                    ),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_ECONOMY) }
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
                        text = LocaleHelper.coinPacksTitle(isPersian),
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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.tv),
                                contentDescription = "TV Ad",
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocaleHelper.freeCoinsTitle(isPersian),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont,
                                    maxLines = 1
                                )
                                Text(
                                    text = LocaleHelper.freeCoinsDesc(isPersian),
                                    color = Color(0xFFDBEAFE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

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
                                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    text = if (isPersian) "+۵۰" else "+50",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------
                // PAID COIN BANNERS: 500, 1200, 3000
                // ------------------------------------------
                CoinPackRow(
                    amountText = if (isPersian) "۵۰۰" else "500",
                    priceText = if (isPersian) "۱۹,۰۰۰ تومان" else "₺39.99",
                    gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_COINS_500) }
                )

                CoinPackRow(
                    amountText = if (isPersian) "۱۲۰۰" else "1200",
                    priceText = if (isPersian) "۳۹,۰۰۰ تومان" else "₺69.99",
                    gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                    badgeText = if (isPersian) "محبوب" else "POPÜLER",
                    badgeColor = Color(0xFFDC2626),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_COINS_1200) }
                )

                CoinPackRow(
                    amountText = if (isPersian) "۳۰۰۰" else "3000",
                    priceText = if (isPersian) "۷۹,۰۰۰ تومان" else "₺129.99",
                    gradientColors = listOf(Color(0xFFFFD700), Color(0xFFD97706)),
                    badgeText = if (isPersian) "بهترین ارزش" else "EN İYİ FİYAT",
                    badgeColor = Color(0xFFDC2626),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_COINS_3000) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRestorePurchases() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPersian) "🔄 بازیابی خریدهای قبلی" else "🔄 Satın Alımları Geri Yükle",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = nunitoFont
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = nunitoFont,
            maxLines = 1,
            softWrap = false
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
    enabled: Boolean = true,
    buttonText: String? = null,
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
                .defaultMinSize(minHeight = 165.dp)
                .shadow(10.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(gradientColors))
                .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 140.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT 2/3 PORTION: 2 Lines of Items Spaced Evenly
                Column(
                    modifier = Modifier
                        .weight(2.1f)
                        .padding(vertical = 4.dp),
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

                    Spacer(modifier = Modifier.height(8.dp))

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
                        .height(115.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // RIGHT 1/3 PORTION: Package Title ABOVE Price Button (All Centered)
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(vertical = 4.dp),
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
                                if (!enabled) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF059669), Color(0xFF047857))
                                    )
                                } else if (isGoldPriceButton) {
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
                            .clickable(enabled = enabled) { onBuy() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonText ?: price,
                            color = if (!enabled || !isGoldPriceButton) Color.White else Color(0xFF0F172A),
                            fontSize = 15.sp,
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
