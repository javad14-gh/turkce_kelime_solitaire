package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnOutline
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont

enum class MessageType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

data class InGameMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val type: MessageType = MessageType.INFO
)

@Composable
fun InGameToastBanner(
    message: InGameMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val appFont = rememberAppFont()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(message?.id) {
        if (message != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 18.dp, end = 18.dp)
            .zIndex(999f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 60 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { -it - 60 },
                animationSpec = tween(220)
            ) + fadeOut(animationSpec = tween(180))
        ) {
            if (message != null) {
                val (borderStrokeColor, bgColors, iconEmoji) = when (message.type) {
                    MessageType.ERROR -> Quadruple(
                        MenuRedBack,
                        listOf(
                            Color(0xFFFF4D4D),
                            Color(0xFFF30000)
                        ),
                        "⚠️"
                    )
                    MessageType.WARNING -> Quadruple(
                        Color(0xFFBD4900),
                        listOf(
                            Color(0xFFFF914D),
                            Color(0xFFFF6505)
                        ),
                        "⚠️"
                    )
                    MessageType.SUCCESS -> Quadruple(
                        Color(0xFF518F00),
                        listOf(
                            Color(0xFF95FF0A),
                            Color(0xFF61AA00)
                        ),
                        "✨"
                    )
                    MessageType.INFO -> Quadruple(
                        Color(0xFF9440DD),
                        listOf(
                            Color(0xFFb67de8), Color(0xFF8C33DB)
                        ),
                        "💡"
                    )
                }

                val layoutDir = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 460.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color.Black,
                                spotColor = Color.Black
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(bgColors))
                            .border(1.5.dp, borderStrokeColor, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Badge icon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape),
                                        //.background(badgeColor.copy(alpha = 0.25f))
                                        //.border(1.dp, borderStrokeColor.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = iconEmoji,
                                        fontSize = 25.sp
                                    )
                                }


                                // Main Message text
                                Text(
                                    text = message.message,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = appFont,
                                    lineHeight = 21.sp,
                                    textAlign = if (isPersian) TextAlign.Right else TextAlign.Left,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C>(
    val first: A,
    val second: B,
    val third: C
)

@Preview(
    name = "Toast Banner - All Types",
    showBackground = true,
    backgroundColor = 0xFF111827,
    widthDp = 400,
    heightDp = 360
)
@Composable
fun InGameToastBannerPreview() {
    val messages = listOf(
        InGameMessage(
            id = 1,
            message = "این یک پیام خطا است",
            type = MessageType.ERROR
        ),
        InGameMessage(
            id = 2,
            message = "این یک پیام هشدار است",
            type = MessageType.WARNING
        ),
        InGameMessage(
            id = 3,
            message = "عملیات با موفقیت انجام شد",
            type = MessageType.SUCCESS
        ),
        InGameMessage(
            id = 4,
            message = "برای ادامه راهنمایی را دنبال کنید",
            type = MessageType.INFO
        )
    )

    Column(
        modifier = Modifier
            .background(Color(0xFF111827))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        messages.forEach { msg ->
            InGameToastBanner(
                message = msg,
                onDismiss = {}
            )
        }
    }
}
