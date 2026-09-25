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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
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

    val scaleFactor by animateFloatAsState(targetValue = if (isHighlighted) 1.05f else 1.0f)

    val glowColor by animateColorAsState(
        targetValue = when {
            isCompleted -> Color(0xFFFF9F0A) // Glowing Amber/Orange for completed slots
            matchedWords.isNotEmpty() -> AccentGold.copy(alpha = 0.7f) // Gold outline when matching words
            isHighlighted -> SecondaryNeon // Teal highlight when card selected
            else -> Color.White.copy(alpha = 0.15f)
        }
    )

    // Smooth, gentle fade-out & shrink when category is completed
    val cardAlpha = remember { Animatable(1f) }
    val cardScale = remember { Animatable(1f) }
    val badgeAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.7f) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            // Step 1: Pop completion badge in and pulse card gently
            launch {
                badgeAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                badgeScale.animateTo(1.08f, tween(300, easing = FastOutSlowInEasing))
                badgeScale.animateTo(1.0f, tween(150, easing = FastOutSlowInEasing))
            }
            launch {
                cardScale.animateTo(1.04f, tween(250, easing = FastOutSlowInEasing))
                cardScale.animateTo(1.0f, tween(200, easing = FastOutSlowInEasing))
            }

            // Step 2: Keep clearly visible and celebrated for 900ms
            delay(900)

            // Step 3: Gentle, slow dissolve of completed category card & badge
            launch {
                cardAlpha.animateTo(0f, tween(1100, easing = FastOutSlowInEasing))
            }
            launch {
                cardScale.animateTo(0.92f, tween(1100, easing = FastOutSlowInEasing))
            }
            launch {
                badgeAlpha.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
            }
        } else {
            cardAlpha.snapTo(1f)
            cardScale.snapTo(1f)
            badgeAlpha.snapTo(0f)
            badgeScale.snapTo(0.7f)
        }
    }

    Box(
        modifier = modifier
            .size(width = 85.dp, height = 110.dp) // Playing card vertical proportions
            .scale(scaleFactor)
            .onGloballyPositioned { coordinates ->
                onBoundsPositioned(slot, coordinates.boundsInRoot())
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // --- 1. BASE EMPTY FELT CONTAINER (Always rendered underneath so green felt reveals as card dissolves) ---
        val emptySlotBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F3B24), Color(0xFF154C30))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(emptySlotBrush, shape = RoundedCornerShape(8.dp))
                .border(
                    width = if (isHighlighted && activeCategory == null) 2.dp else 1.dp,
                    color = if (isHighlighted && activeCategory == null) SecondaryNeon else Color.White.copy(alpha = 0.2f),
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
                    alpha = 0.4f
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPersian) "کارت دسته" else "Kat. Eşle",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = if (isPersian) 10.sp else 9.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- 2. ACTIVE CATEGORY CARD (Dissolves smoothly with cardAlpha & cardScale when completed) ---
        if (activeCategory != null) {
            val activeSlotBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1))
            )
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
                    .shadow(6.dp, RoundedCornerShape(8.dp), clip = false)
                    .background(activeSlotBrush, shape = RoundedCornerShape(8.dp))
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
                            .background(AccentGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeCategory.name,
                            color = Color.Black,
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
                            color = Color.DarkGray,
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
                                    color = Color.Black,
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

        // --- 3. CATEGORY COMPLETED CELEBRATION BADGE (Below slot with slight overlap, NO category name) ---
        if (badgeAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
                    .zIndex(20f)
                    .graphicsLayer {
                        alpha = badgeAlpha.value
                        scaleX = badgeScale.value
                        scaleY = badgeScale.value
                    }
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFB800), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "👑",
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = LocaleHelper.categoryCompleted(isPersian),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
