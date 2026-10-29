package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnMid
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextSecondary
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import java.util.Locale

// Marketing numbers in one place (change here, not inside the UI)
private const val STARTER_DISCOUNT_PERCENT = 70
private const val STARTER_FAKE_OLD_PRICE_TOMANS = 330_000
private const val STARTER_TRY_PRICE = "₺99.99"
private const val STARTER_TRY_OLD_PRICE = "₺330"

@Composable
fun StarterPackDialog(
    onDismiss: () -> Unit,
    onBuy: () -> Unit
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val font = rememberNunitoFont()

    val priceTomans = remember {
        MyketBillingConfig.ALL_PRODUCTS
            .first { it.sku == MyketBillingConfig.SKU_STARTER_PACK }.priceTomans
    }
    fun toman(v: Int) = LocaleHelper.formatNumber(String.format(Locale.US, "%,d", v), true) + " تومان"
    val priceText = if (isPersian) toman(priceTomans) else STARTER_TRY_PRICE
    val oldPriceText = if (isPersian) toman(STARTER_FAKE_OLD_PRICE_TOMANS) else STARTER_TRY_OLD_PRICE

    val pulse = rememberInfiniteTransition(label = "starterPulse")
    val buttonScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "starterPulseScale"
    )

    PopupFrame(
        title = if (isPersian) "بسته شروع" else "BAŞLANGIÇ PAKETİ",
        onDismiss = onDismiss,
        titleSize = if (isPersian) 28.sp else 22.sp
    ) {
        // Discount ribbon
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDC2626))
                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (isPersian)
                    "٪${LocaleHelper.formatNumber(STARTER_DISCOUNT_PERCENT, true)} تخفیف ویژه (فقط یک‌بار)"
                else "%$STARTER_DISCOUNT_PERCENT ÖZEL İNDİRİM (TEK SEFERLİK)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = font
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isPersian) "با این بسته مراحل را راحت‌تر پشت سر بگذار!"
            else "Bu paketle seviyeleri çok daha kolay tamamlayın!",
            color = PopupTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StarterItemRow(R.drawable.coins, if (isPersian) "۲۰۰۰ سکه" else "2000 Altın", null)
            StarterItemRow(
                R.drawable.no_ads,
                if (isPersian) "حذف دائمی تبلیغات" else "Tüm Reklamları Kaldır",
                if (isPersian) "دائمی" else "Kalıcı",
                highlight = true
            )
            StarterItemRow(R.drawable.hint, if (isPersian) "کارت راهنما" else "İpucu Kartı", LocaleHelper.formatNumber("3×", isPersian))
            StarterItemRow(R.drawable.undo, if (isPersian) "بازگشت حرکت" else "Geri Alma Kartı", LocaleHelper.formatNumber("3×", isPersian))
            StarterItemRow(R.drawable.joker, if (isPersian) "کارت جوکر" else "Joker Kartı", LocaleHelper.formatNumber("3×", isPersian))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = (if (isPersian) "ارزش واقعی: " else "Değeri: ") + oldPriceText,
            color = PopupTextSecondary.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.LineThrough,
            fontFamily = font
        )

        Spacer(modifier = Modifier.height(8.dp))

        CandyButton(
            onClick = {
                GameSettingsManager.playButtonClickSound(context)
                onBuy()
            },
            modifier = Modifier
                .scale(buttonScale)
                .fillMaxWidth(0.92f),
            corner = 18.dp,
            elevation = 10.dp
        ) {
            val outline = Color(0x802D5A0E)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = R.drawable.gift),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedText(
                    text = priceText,
                    textColor = Color.White,
                    outlineColor = outline,
                    outlineWidth = 6f,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    shadowColor = outline.copy(alpha = 0.6f),
                    shadowOffsetY = 3f
                )
            }
        }
    }
}

@Composable
private fun StarterItemRow(
    iconRes: Int,
    title: String,
    badge: String?,
    highlight: Boolean = false
) {
    val font = rememberNunitoFont()
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (highlight) Color(0xFFD1FAE5) else PopupButtonBg)
            .border(1.dp, if (highlight) Color(0xFF34D399) else MenuBtnOutline.copy(alpha = 0.25f), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = PopupTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = font,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (highlight) Color(0xFF059669) else MenuBtnMid)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = font)
            }
        }
    }
}