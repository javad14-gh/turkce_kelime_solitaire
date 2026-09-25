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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

@Composable
fun StarterPackDialog(
    onDismiss: () -> Unit,
    onBuy: () -> Unit
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .zIndex(500f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF3B0764), Color(0xFF240642), Color(0xFF140326))
                    )
                )
                .border(2.5.dp, AccentGold, RoundedCornerShape(26.dp))
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Gold Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFB8860B), Color(0xFF78350F))
                            )
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.crown),
                            contentDescription = "Crown",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "بسته شروع شگفت‌انگیز" else "BAŞLANGIÇ FIRSATI",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }

                    // Cancel 'X' Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Ribbon: 70% Discount Badge
                Box(
                    modifier = Modifier
                        .offset(y = (-10).dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFDC2626), Color(0xFFEF4444), Color(0xFFB91C1C))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPersian) "٪۷۰ تخفیف ویژه (فقط یک‌بار)" else "%70 ÖZEL İNDİRİM (TEK SEFERLİK)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }

                Text(
                    text = if (isPersian) "با این بسته فوق‌العاده، مراحل را به آسانی پشت سر بگذارید!" else "Bu paketle seviyeleri çok daha kolay tamamlayın!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    fontFamily = nunitoFont
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Items list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StarterPackItemRow(
                        iconRes = R.drawable.coins,
                        title = if (isPersian) "۲۰۰۰ سکه طلای بازی" else "2000 Altın",
                        badge = if (isPersian) "ارزش ۱۰۰ هزار تومان" else "Büyük Değer"
                    )
                    StarterPackItemRow(
                        iconRes = R.drawable.no_ads,
                        title = if (isPersian) "حذف دائمی تمام تبلیغات" else "Tüm Reklamları Kaldır",
                        badge = if (isPersian) "دائمی" else "Kalıcı",
                        isHighlight = true
                    )
                    StarterPackItemRow(
                        iconRes = R.drawable.hint,
                        title = if (isPersian) "۳ عدد کارت راهنما" else "3x İpucu Kartı",
                        badge = "۳×"
                    )
                    StarterPackItemRow(
                        iconRes = R.drawable.undo,
                        title = if (isPersian) "۳ عدد بازگشت حرکت" else "3x Geri Alma Kartı",
                        badge = "۳×"
                    )
                    StarterPackItemRow(
                        iconRes = R.drawable.joker,
                        title = if (isPersian) "۳ عدد کارت جوکر" else "3x Joker Kartı",
                        badge = "۳×"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price comparison: Old strikethrough price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isPersian) "ارزش واقعی: ۳۳۰,۰۰۰ تومان" else "Değeri: ₺330",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.LineThrough,
                        fontFamily = nunitoFont
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pulsating 3D Gold Buy Button
                Box(
                    modifier = Modifier
                        .scale(buttonScale)
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .shadow(14.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
                            )
                        )
                        .padding(2.5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF78350F))
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        )
                        .clickable { onBuy() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gift),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian) "خرید فوری - ۹۹,۰۰۰ تومان" else "Hemen Al - ₺99.99",
                            color = Color(0xFF451A03),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarterPackItemRow(
    iconRes: Int,
    title: String,
    badge: String,
    isHighlight: Boolean = false
) {
    val nunitoFont = rememberNunitoFont()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHighlight) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF10B981).copy(alpha = 0.25f), Color(0xFF047857).copy(alpha = 0.35f))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF581C87).copy(alpha = 0.35f), Color(0xFF3B0764).copy(alpha = 0.45f))
                    )
                }
            )
            .border(
                1.dp,
                if (isHighlight) Color(0xFF34D399) else Color(0xFFFFD700).copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = nunitoFont
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isHighlight) Color(0xFF059669) else Color(0xFFF59E0B)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
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
