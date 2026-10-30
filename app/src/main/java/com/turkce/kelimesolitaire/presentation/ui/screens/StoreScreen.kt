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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.lerp
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.components.candyGloss
import com.turkce.kelimesolitaire.presentation.ui.components.specularGlint
import com.turkce.kelimesolitaire.presentation.ui.components.glassReflection
import com.turkce.kelimesolitaire.presentation.ui.components.darker
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import com.turkce.kelimesolitaire.presentation.ui.components.CoinCounter
import com.turkce.kelimesolitaire.presentation.ui.theme.mainBg
import com.turkce.kelimesolitaire.presentation.ui.components.TopBarIconButton
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain

val inner = RoundedCornerShape(19.dp)
val outer = RoundedCornerShape(24.dp)

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
                mainBg
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Coins Display: 3D Coin on the outside, box extending to the right (like menu buttons)
                    CoinCounter(
                        coins,
                        isPersian,
                    )

                    // 2. Settings Menu Icon (Right) - 3D Button like Sound & Vibration (size 40dp, gear 34dp)
                    TopBarIconButton(
                        iconRes = R.drawable.cancel,
                        contentDescription = "Cancel",
                        onClose
                    )
                }
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
                            .background(Color(0xFF9440DD))
                            .candyGloss(corner = 24.dp, inset = 2.5.dp)
                            .padding(7.dp)
                            .clip(inner)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFD6DDFF),Color(0xFFD6DDFF))
                                )
                            )
                            //.glassReflection()
                            .border(
                                2.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.12f)
                                    )
                                ),
                                inner)
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
                                    fontFamily = nunitoFont,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LocaleHelper.removeAdsDesc(isPersian),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .shadow(6.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFF43F5E),
                                                Color(0xFFBE123C)
                                            )
                                        )
                                    )
                                    .candyGloss(14.dp,2.dp )
                                    .specularGlint()
                                    .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                                    .clickable { onPurchaseSku(MyketBillingConfig.SKU_REMOVE_ADS) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedText(
                                    text = if (isPersian) "۳۹,۰۰۰ تومان" else "₺49.99",
                                    textColor = Color.White,
                                    outlineColor = Color(0xFFBE123C).darker(),
                                    outlineWidth = 6f,
                                    fontSize = 15.sp,
                                    shadowColor = Color(0xFFBE123C).darker().copy(0.6f),
                                    fontWeight = FontWeight.Black,
                                    //fontFamily = nunitoFont
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
                        gradientColors = listOf(Color(0xFFD6DDFF),Color(0xFFD6DDFF)),
                        borderColor = Color(0xFFFFA51F),
                        titleColor = Color(0xFFFFA51F),
                        isGoldPriceButton = false,
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
                    gradientColors = listOf(Color(0xFFFFF6E6),Color(0xFFEADFC8)),
                    borderColor = Color(0xFF99E17A),
                    titleColor = Color(0xFF99E17A),
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
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_MEGA) },
                )

                // ------------------------------------------
                // COMBO 1: SÜPER PAKET (بسته ویژه)
                // ------------------------------------------
                ComboPackCard(
                    title = if (isPersian) "بسته ویژه" else "SÜPER PAKET",
                    badge = if (isPersian) "پرفروش" else "ÇOK SATAN",
                    price = if (isPersian) "۸۹,۰۰۰ تومان" else "₺89.99",
                    gradientColors = listOf(Color(0xFFD6DDFF),Color(0xFFD6DDFF)),
                    borderColor = Color(0xFFFFD21F),
                    titleColor = Color(0xFFFFD21F),
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
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_SPECIAL) },
                )

                // ------------------------------------------
                // COMBO 2: AVANTAJ PAKETİ (بسته اقتصادی)
                // ------------------------------------------
                ComboPackCard(
                    title = if (isPersian) "بسته اقتصادی" else "AVANTAJ PAKETİ",
                    badge = if (isPersian) "به‌صرفه" else "FIRSAT",
                    price = if (isPersian) "۴۹,۰۰۰ تومان" else "₺49.99",
                    gradientColors = listOf(Color(0xFFD6DDFF),Color(0xFFD6DDFF)),
                    borderColor = Color(0xFFFF66C4),
                    titleColor = Color(0xFFFF66C4),
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
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_BUNDLE_ECONOMY) },
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
                        .background(Color(0xFFCC0000)
                        )
                        .border(1.5.dp, Color(0xFFCC0000), RoundedCornerShape(20.dp))
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
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFA3E635),
                                            Color(0xFF65A30D),
                                            Color(0xFF4D7C0F)
                                        )
                                    )
                                )
                                .candyGloss(corner = 14.dp, inset = 2.dp)
                                .specularGlint()
                                .border(1.dp, Color(0x802D5A0E), RoundedCornerShape(14.dp))
                                .clickable { onWatchAdForCoins() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val btnOutline = Color(0x802D5A0E)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.play),
                                    contentDescription = "Watch Ad",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedText(
                                    text = if (isPersian) "+۵۰" else "+50",
                                    textColor = Color.White,
                                    outlineColor = btnOutline,
                                    outlineWidth = 6f,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    shadowColor = btnOutline.copy(alpha = 0.6f),
                                    shadowOffsetY = 3f
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
                    //gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_COINS_500) }
                )

                CoinPackRow(
                    amountText = if (isPersian) "۱۲۰۰" else "1200",
                    priceText = if (isPersian) "۳۹,۰۰۰ تومان" else "₺69.99",
                    //gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                    badgeText = if (isPersian) "محبوب" else "POPÜLER",
                    badgeColor = Color(0xFFDC2626),
                    onBuy = { onPurchaseSku(MyketBillingConfig.SKU_COINS_1200) }
                )

                CoinPackRow(
                    amountText = if (isPersian) "۳۰۰۰" else "3000",
                    priceText = if (isPersian) "۷۹,۰۰۰ تومان" else "₺129.99",
                    //gradientColors = listOf(Color(0xFFFFD700), Color(0xFFD97706)),
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
            color = Color(0xFFffffff),
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
    onBuy: () -> Unit,
    titleColor: Color = Color(0xFF5271ff)
) {
    val nunitoFont = rememberNunitoFont()
    val frameTop = lerp(borderColor, Color.White, 0.35f)
    val frameBottom = lerp(borderColor, Color.Black, 0.30f)
    val panelShape = RoundedCornerShape(16.dp)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(top = if (badge != null) 6.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(10.dp, outer)
                .clip(outer)
                .background(borderColor)
                .candyGloss(corner = 24.dp, inset = 4.5.dp)   // گلاس روی قاب، پشت پنل
                .padding(8.dp)                                // قاب کلفت‌تر تا گلاس دیده بشه
                .clip(inner)
                .background(Brush.verticalGradient(listOf(Color(0xFFFFF6E6), Color(0xFFEADFC8))))
                //.glassReflection()
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.12f)
                        )
                    ),
                    inner
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 140.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT 2/3 PORTION: 2 Lines of Items Spaced Evenly
                Box(
                    modifier = Modifier
                        .weight(2.1f)
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
                        .clip(panelShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    borderColor.copy(alpha = 0.28f),   // بالا کمی پررنگ‌تر
                                    borderColor.copy(alpha = 0.16f)    // پایین ملایم‌تر
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    borderColor.copy(alpha = 0.25f)
                                )
                            ),
                            panelShape
                        )
                        .padding(vertical = 6.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                }}

                // Vertical Separator Line
//                Spacer(
//                    modifier = Modifier
//                        .width(1.dp)
//                        .height(115.dp)
//                        .background(Color.White.copy(alpha = 0.15f))
//                )

                // RIGHT 1/3 PORTION: Package Title ABOVE Price Button (All Centered)
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedText(
                        text = title,
                        textColor =  titleColor,
                        outlineColor = titleColor.darker(),
                        shadowColor = titleColor.darker().copy(alpha=0.6f),
                        shadowOffsetY = 1f,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        outlineWidth = 7f,
                        //fontFamily = nunitoFont,
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
                                        listOf(titleColor, Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFA3E635),
                                            Color(0xFF65A30D),
                                            Color(0xFF4D7C0F)
                                        )
                                    )
                                }
                            )
                            .candyGloss(corner = 14.dp, inset = 2.dp)
                            .specularGlint()
                            .border(1.dp, Color(0x802D5A0E), RoundedCornerShape(14.dp))
                            .clickable(enabled = enabled) { onBuy() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val priceOutline = when {
                            !enabled -> Color(0x802D5A0E)
                            isGoldPriceButton -> Color(0xFF7C2D12)
                            else -> Color(0x802D5A0E)        // سبز تیره
                        }
                        OutlinedText(
                            text = buttonText ?: price,
                            textColor = Color.White,
                            outlineColor = priceOutline,
                            outlineWidth = 6f,
                            fontSize = 15.sp,
                            shadowColor = priceOutline.copy(alpha = 0.6f),
                            shadowOffsetY = 3f
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
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFDC2626),
    onBuy: () -> Unit
) {
    val nunitoFont = rememberNunitoFont()
    val gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.padding(top = if (badgeText != null) 6.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(10.dp, outer)
                .clip(outer)
                .background(Color(0xFF99DAFF))
                .padding(7.dp)
                .clip(inner)
                .background(Color(0xFF99DAFF).copy(alpha = 0.12f))
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.12f)
                        )
                    ),
                    inner)
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
                        .candyGloss(14.dp,2.dp)
                        .specularGlint()
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .clickable { onBuy() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = priceText,
                        textColor = Color.White,
                        outlineColor = Color(0xFFF59E0B).darker(),
                        outlineWidth = 6f,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        shadowColor = Color(0xFFF59E0B).darker().copy(0.6f),
                        shadowOffsetY = 3f
                        //fontFamily = nunitoFont
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
