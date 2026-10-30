package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import androidx.compose.ui.graphics.ColorFilter
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.CategoryActiveBannerBg
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotActiveBg
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotBg
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotContent
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotInnerShadow
import com.turkce.kelimesolitaire.presentation.ui.theme.CategorySlotMatchedBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.CardFaceText
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CategoryDropZone(
    slot: FoundationSlot,
    totalWords: Int,
    isHighlighted: Boolean, // Highlight outline if a card is selected
    onTap: () -> Unit,
    onBoundsPositioned: (FoundationSlot, Rect) -> Unit, // Window coordinates bounds reporter
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()
    val activeCategory = slot.activeCategory
    val matchedWords = slot.matchedWords
    val isCompleted = matchedWords.size == totalWords && totalWords > 0
    // Feedback triggers: pulse + sparkles on each new matched word, sticker on completion
    var sparkleTrigger by remember { mutableIntStateOf(0) }
    var stickerTrigger by remember { mutableIntStateOf(0) }
    val slotPulse = remember { Animatable(1f) }
    var lastCount by remember { mutableIntStateOf(matchedWords.size) }

    var comboTrigger by remember { mutableIntStateOf(0) }
    var lastCategoryId by remember { mutableStateOf(slot.activeCategory?.id) }

// 1) New matched word(s) -> pulse + sparkles (+ combo when 2 or more arrive at once)
    LaunchedEffect(matchedWords.size) {
        val added = matchedWords.size - lastCount
        if (added > 0) {
            sparkleTrigger++
            if (added >= 2 && !isCompleted) comboTrigger++
            slotPulse.snapTo(1f)
            slotPulse.animateTo(1.12f, tween(110, easing = FastOutSlowInEasing))
            slotPulse.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
        }
        lastCount = matchedWords.size
    }

// 2) Category card placed -> pulse + sparkles
    LaunchedEffect(slot.activeCategory?.id) {
        val id = slot.activeCategory?.id
        if (id != null && id != lastCategoryId) {
            sparkleTrigger++
            slotPulse.snapTo(1f)
            slotPulse.animateTo(1.12f, tween(110, easing = FastOutSlowInEasing))
            slotPulse.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
        }
        lastCategoryId = id
    }

    val scaleFactor by animateFloatAsState(targetValue = if (isHighlighted) 1.05f else 1.0f)

    val glowColor by animateColorAsState(
        targetValue = when {
            isCompleted -> CategorySlotMatchedBorder
            matchedWords.isNotEmpty() -> CategorySlotMatchedBorder // هماهنگ با نوار بالای کارت فعال دسته‌بندی
            isHighlighted -> SecondaryNeon // Teal highlight when card selected
            else -> Color.White.copy(alpha = 0.15f)
        }
    )

    // Smooth, gentle fade-out & shrink when category is completed
    val cardAlpha = remember { Animatable(1f) }
    val cardScale = remember { Animatable(1f) }


    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            stickerTrigger++
            sparkleTrigger++   // با تکمیل دسته، جرقه‌ی دوم هم می‌آید
            // Step 1: pulse card gently
            launch {
                cardScale.animateTo(1.04f, tween(250, easing = FastOutSlowInEasing))
                cardScale.animateTo(1.0f, tween(200, easing = FastOutSlowInEasing))
            }

            // Step 2: keep clearly visible for 900ms
            delay(900)

            // Step 3: gentle dissolve
            launch { cardAlpha.animateTo(0f, tween(1100, easing = FastOutSlowInEasing)) }
            launch { cardScale.animateTo(0.92f, tween(1100, easing = FastOutSlowInEasing)) }
        } else {
            cardAlpha.snapTo(1f)
            cardScale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .size(width = 85.dp, height = 110.dp) // Playing card vertical proportions
            .scale(scaleFactor * slotPulse.value)
            .onGloballyPositioned { coordinates ->
                onBoundsPositioned(slot, coordinates.boundsInRoot())
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // --- 1. BASE EMPTY CONTAINER (Theme colors from Color.kt) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(CategorySlotBg)
                .drawWithContent {
                    drawContent()
                    // ۱. سایه شیب‌دار عمیق از بالا به پایین (ایجاد حس گودی و سوراخ فیزیکی)
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to CategorySlotInnerShadow.copy(alpha = 0.50f),
                            0.20f to CategorySlotInnerShadow.copy(alpha = 0.22f),
                            0.50f to Color.Transparent,
                            startY = 0f,
                            endY = size.height * 0.50f
                        )
                    )
                    // ۲. سایه شیب‌دار از لبه چپ
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to CategorySlotInnerShadow.copy(alpha = 0.35f),
                            0.20f to CategorySlotInnerShadow.copy(alpha = 0.12f),
                            0.45f to Color.Transparent,
                            startX = 0f,
                            endX = size.width * 0.40f
                        )
                    )
                    // ۳. سایه نرم از لبه راست
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0.60f to Color.Transparent,
                            0.85f to CategorySlotInnerShadow.copy(alpha = 0.10f),
                            1f to CategorySlotInnerShadow.copy(alpha = 0.25f),
                            startX = size.width * 0.60f,
                            endX = size.width
                        )
                    )
                    // ۴. لبه براق پایینی (Bevel) برای القای عمق لبه سوراخ
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.82f to Color.Transparent,
                            1f to Color.White.copy(alpha = 0.40f),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                }
                .border(
                    width = if (isHighlighted && activeCategory == null) 2.dp else 1.2.dp,
                    color = if (isHighlighted && activeCategory == null) SecondaryNeon else CategorySlotBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(enabled = activeCategory == null) { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = "Crown",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(CategorySlotContent.copy(alpha = 0.65f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPersian) "کارت دسته" else "Kat. Eşle",
                    color = CategorySlotContent,
                    fontSize = if (isPersian) 10.sp else 9.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- 2. ACTIVE CATEGORY CARD (Dissolves smoothly with cardAlpha & cardScale when completed) ---
        if (activeCategory != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = cardAlpha.value
                        scaleX = cardScale.value
                        scaleY = cardScale.value
                    }
                    .shadow(4.dp, RoundedCornerShape(8.dp), clip = false)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CategorySlotActiveBg, shape = RoundedCornerShape(8.dp))
                    .border(
                        width = when {
                            isCompleted -> 3.dp
                            isHighlighted -> 2.dp
                            matchedWords.isNotEmpty() -> 1.5.dp
                            else -> 1.dp
                        },
                        color = glowColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTap() }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Gold Category Banner inside playing card top section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(CategoryActiveBannerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeCategory.name,
                            color = CardFaceText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = nunitoFont,
                            maxLines = 2,
                            lineHeight = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Playable Slot Card Body
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        // Match counter in top-right corner
                        Text(
                            text = "${LocaleHelper.formatNumber(matchedWords.size, isPersian)}/${LocaleHelper.formatNumber(totalWords, isPersian)}",
                            color = CardFaceText.copy(alpha = 0.75f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = nunitoFont,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

                        if (isCompleted) {
                            Image(
                                painter = painterResource(id = R.drawable.crown),
                                contentDescription = "Completed Crown",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .size(14.dp)
                            )
                        }

                        // Display actual last matched word in the list, or empty Crown symbol
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val lastWord = matchedWords.lastOrNull()
                            if (lastWord != null) {
                                val length = lastWord.wordText.length
                                val dropZoneFontSize = when {
                                    length >= 12 -> 8.5.sp
                                    length >= 10 -> 9.5.sp
                                    length >= 8 -> 10.5.sp
                                    else -> 12.sp
                                }
                                val dropZoneLetterSpacing = when {
                                    length >= 10 -> (-0.5).sp
                                    else -> 0.sp
                                }
                                Text(
                                    text = lastWord.wordText,
                                    color = CardFaceText,
                                    fontSize = dropZoneFontSize,
                                    fontWeight = FontWeight.W800,
                                    fontFamily = nunitoFont,
                                    maxLines = 1,
                                    softWrap = false,
                                    letterSpacing = dropZoneLetterSpacing,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.crown),
                                    contentDescription = "Category Crown",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
// --- 3. FEEDBACK LAYERS ---
        SparkleBurst(
            trigger = sparkleTrigger,
            count = if (isCompleted) 14 else 6,
            maxDistanceDp = if (isCompleted) 70f else 46f,
            modifier = Modifier.zIndex(20f)
        )
        CompletionSticker(
            trigger = stickerTrigger,
            isPersian = isPersian,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-30).dp)
                .zIndex(30f)
        )
        CompletionSticker(
            trigger = comboTrigger,
            isPersian = isPersian,
            isCombo = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-30).dp)
                .zIndex(31f)
        )
    }
}
