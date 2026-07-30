package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen

@Composable
fun CategoryDropZone(
    slot: FoundationSlot,
    totalWords: Int,
    isHighlighted: Boolean, // Highlight outline if a card is selected
    onTap: () -> Unit,
    onBoundsPositioned: (FoundationSlot, Rect) -> Unit, // Window coordinates bounds reporter
    modifier: Modifier = Modifier
) {
    val scaleFactor by animateFloatAsState(targetValue = if (isHighlighted) 1.05f else 1.0f)
    val activeCategory = slot.activeCategory
    val matchedWords = slot.matchedWords
    val isCompleted = matchedWords.size == totalWords && totalWords > 0

    val glowColor by animateColorAsState(
        targetValue = when {
            isCompleted -> SuccessGreen
            isHighlighted -> SecondaryNeon
            else -> Color.White.copy(alpha = 0.15f)
        }
    )

    if (activeCategory == null) {
        // --- 1. INACTIVE/EMPTY SLOT (Green felt container waiting for category activation) ---
        Box(
            modifier = modifier
                .size(width = 85.dp, height = 110.dp) // Playing card vertical proportions
                .scale(scaleFactor)
                .background(Color(0xFF154C30), shape = RoundedCornerShape(8.dp))
                .border(
                    width = if (isHighlighted) 2.dp else 1.dp,
                    color = if (isHighlighted) SecondaryNeon else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .onGloballyPositioned { coordinates ->
                    onBoundsPositioned(slot, coordinates.boundsInRoot())
                }
                .clickable { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "👑",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kat. Eşle",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // --- 2. ACTIVE SLOT (Category playing card style) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
                .size(width = 85.dp, height = 110.dp)
                .scale(scaleFactor)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .border(
                    width = if (isHighlighted || isCompleted) 2.dp else 1.dp,
                    color = glowColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .onGloballyPositioned { coordinates ->
                    onBoundsPositioned(slot, coordinates.boundsInRoot())
                }
                .clickable { onTap() }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Gold Category Banner inside playing card top section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(AccentGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeCategory.name,
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
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
                        text = "${matchedWords.size}/$totalWords",
                        color = Color.DarkGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    // Complete Crown Icon
                    if (isCompleted) {
                        Text(
                            text = "👑",
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }

                    // Display actual last matched word in the list, or empty Crown symbol
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val lastWord = matchedWords.lastOrNull()
                        if (lastWord != null) {
                            Text(
                                text = lastWord.wordText,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        } else {
                            Text(
                                text = "👑",
                                color = AccentGold.copy(alpha = 0.8f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
