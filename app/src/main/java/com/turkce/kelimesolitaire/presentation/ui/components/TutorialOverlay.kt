package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType

@Composable
fun BoosterUnlockDialog(
    tutorialType: TutorialType,
    isPersian: Boolean,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val title = when (tutorialType) {
        TutorialType.UNDO_UNLOCK -> if (isPersian) "قابلیت بازگشت باز شد! 🎉" else "Geri Al Özelliği Açıldı! 🎉"
        TutorialType.HINT_UNLOCK -> if (isPersian) "راهنما در مراحل سخت! 💡" else "Zor Seviye İpucu Açıldı! 💡"
        TutorialType.JOKER_UNLOCK -> if (isPersian) "برگ برنده: کارت جوکر! 🃏" else "Süper Kart: Joker Açıldı! 🃏"
        else -> ""
    }

    val description = when (tutorialType) {
        TutorialType.UNDO_UNLOCK -> if (isPersian) {
            "از این پس می‌توانید آخرین حرکت خود را برگردانید.\nبرای شروع، ۱ استفاده کاملاً رایگان هدیه به شما! 🎁"
        } else {
            "Artık yaptığınız son hamleyi geri alabilirsiniz.\nBaşlangıç için ilk kullanım tamamen ÜCRETSİZ! 🎁"
        }
        TutorialType.HINT_UNLOCK -> if (isPersian) {
            "به مراحل سخت بازی رسیدید! دکمه راهنما بهترین حرکت ممکن را به شما نشان می‌دهد.\n۱ راهنمای رایگان هدیه شما! 🎁"
        } else {
            "Zor seviyelere ulaştınız! İpucu düğmesi size en uygun hamleyi gösterir.\nİlk ipucunuz tamamen ÜCRETSİZ! 🎁"
        }
        TutorialType.JOKER_UNLOCK -> if (isPersian) {
            "مرحله خیلی سخت! کارت جوکر در هر دسته‌ای می‌نشیند و راه را باز می‌کند.\n۱ کارت جوکر رایگان هدیه شما! 🎁"
        } else {
            "Çok Zor seviye! Joker kartı her kategoriyle eşleşir ve tıkanıklığı çözer.\nİlk kullanım tamamen ÜCRETSİZ! 🎁"
        }
        else -> ""
    }

    val buttonText = if (isPersian) "متوجه شدم و دریافت هدیه 🎁" else "Anladım ve Hediyeyi Al 🎁"

    // Backdrop with Dark Blur feeling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Block outside clicks */ },
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFD97706))
                    ),
                    RoundedCornerShape(26.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon in glowing pulsating circle
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(80.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFD97706))
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (tutorialType) {
                    TutorialType.UNDO_UNLOCK -> UndoIcon(size = 46.dp)
                    TutorialType.HINT_UNLOCK -> HintIcon(size = 46.dp)
                    TutorialType.JOKER_UNLOCK -> JokerIcon(size = 46.dp)
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD700),
                fontFamily = fontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badge Free Gift
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isPersian) "🎁 ۱ بار استفاده رایگان" else "🎁 1 Kullanım Ücretsiz",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399),
                    fontFamily = fontFamily
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            Text(
                text = description,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0),
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B))
                        )
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    fontFamily = fontFamily
                )
            }
        }
    }
}

@Composable
fun Level1InteractiveBanner(
    step: Int,
    isPersian: Boolean,
    fontFamily: FontFamily,
    onSkip: () -> Unit
) {
    val message = when (step) {
        0 -> if (isPersian) {
            "👑 کارت دسته را بکشید و در یکی از جایگاه‌های خالی بالا بگذارید"
        } else {
            "👑 Kategori kartını yukarıdaki boş alana sürükleyip bırakın"
        }
        1 -> if (isPersian) {
            "✨ آفرین! حالا کلمات مربوط به این دسته را روی آن قرار دهید"
        } else {
            "✨ Harika! Şimdi bu kategoriye ait kelimeleri üzerine yerleştirin"
        }
        else -> if (isPersian) {
            "🃏 اگر حرکتی ندارید، روی دسته کارت‌های سمت راست بزنید"
        } else {
            "🃏 Hamle kalmadıysa sağdaki desteden kart çekin"
        }
    }

    val skipText = if (isPersian) "رد کردن آموزش" else "Atla"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.95f),
                        Color(0xFF1E293B).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFF38BDF8), Color(0xFFFFD700))
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC),
                fontFamily = fontFamily,
                modifier = Modifier.weight(1f),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = skipText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                fontFamily = fontFamily,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkip() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}
