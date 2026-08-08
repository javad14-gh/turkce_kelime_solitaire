package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.ErrorRed
import kotlin.math.roundToInt

@Composable
fun WordCard(
    card: SolitaireCard,
    isSelected: Boolean,
    isShaking: Boolean,
    isShattering: Boolean = false,
    isDragged: Boolean,
    dragOffset: Offset,
    isInteractionEnabled: Boolean,
    onTap: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFaceUp = card.isFaceUp
    val nunitoFont = rememberNunitoFont()
    
    // Wrap callbacks and value arguments in rememberUpdatedState to prevent capturing stale values
    val currentInteractionEnabled by rememberUpdatedState(isInteractionEnabled)
    val currentDragOffset by rememberUpdatedState(dragOffset)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    // Bounds calculations
    var cardPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Shake keyframe animation for matches errors
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            shakeX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    -20f at 50
                    20f at 150
                    -15f at 250
                    15f at 350
                    -8f at 450
                }
            )
        }
    }

    // Shatter & Crumble animation for exposed Joker cards
    val shatterAlpha = remember { Animatable(1f) }
    val shatterScale = remember { Animatable(1f) }
    val shatterOffsetY = remember { Animatable(0f) }
    val shatterRotation = remember { Animatable(0f) }

    LaunchedEffect(isShattering) {
        if (isShattering) {
            launch { shatterAlpha.animateTo(0f, tween(600, easing = LinearOutSlowInEasing)) }
            launch { shatterScale.animateTo(0.3f, tween(600, easing = LinearOutSlowInEasing)) }
            launch { shatterOffsetY.animateTo(140f, tween(600, easing = LinearOutSlowInEasing)) }
            launch { shatterRotation.animateTo(30f, tween(600, easing = LinearOutSlowInEasing)) }
        }
    }

    val scale = if (isDragged) 1.15f else if (isSelected) 1.05f else 1.0f
    val elevation = if (isDragged) 16.dp else if (isSelected) 6.dp else 3.dp
    
    val isJoker = card.categoryId == "joker_wildcard"

    // Board outline styling: category cards get a gold border when face up!
    val borderColor = when {
        isShaking -> ErrorRed
        isJoker -> Color(0xFF7E22CE) // Vibrant Purple Border for Joker!
        isSelected -> AccentGold
        !isFaceUp -> Color.White // Crisp border for card backs
        card.isCategory -> AccentGold.copy(alpha = 0.8f) // Gold outline for category cards
        else -> Color.DarkGray.copy(alpha = 0.3f)
    }

    val cardBrush = when {
        !isFaceUp -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
        )
        isJoker -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEF08A), // Vibrant golden yellow top
                Color(0xFFFDE047), // Deep yellow middle
                Color(0xFFEAB308)  // Rich golden bottom
            )
        )
        card.isCategory -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1))
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .zIndex(if (isDragged) 100f else 0f)
            .graphicsLayer {
                if (isShattering) {
                    alpha = shatterAlpha.value
                    scaleX = shatterScale.value
                    scaleY = shatterScale.value
                    translationY = shatterOffsetY.value
                    rotationZ = shatterRotation.value
                }
            }
            // 1. Measure the static layout slot bounds (placed BEFORE drag offset!)
            .onGloballyPositioned { coordinates ->
                if (!isDragged) {
                    cardPositionInRoot = coordinates.positionInRoot()
                    cardSize = coordinates.size
                }
            }
            .offset {
                IntOffset(
                    shakeX.value.roundToInt(),
                    0
                )
            }
            // 2. Apply drag offset here (BEFORE gesture detection and clicks so the hit area moves with the card!)
            .offset {
                IntOffset(
                    x = dragOffset.x.roundToInt(),
                    y = dragOffset.y.roundToInt()
                )
            }
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(8.dp), clip = false)
            .background(cardBrush, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isJoker || isSelected || isShaking || !isFaceUp || card.isCategory) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isFaceUp) {
                    Modifier
                        .pointerInput(card.id) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = true)
                                down.consume() // Consume Down event immediately to prevent touch leaks to overlapping cards!
                                
                                var isDragging = false
                                
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val anyPressed = event.changes.any { it.pressed }
                                        if (!anyPressed) {
                                            break
                                        }
                                        
                                        val dragChange = event.changes.firstOrNull { it.id == down.id }
                                        if (dragChange != null) {
                                            if (dragChange.isConsumed) {
                                                break
                                            }
                                            
                                            val dragAmount = dragChange.position - dragChange.previousPosition
                                            if (dragAmount.getDistanceSquared() > 0.5f) {
                                                if (!isDragging) {
                                                    isDragging = true
                                                    if (currentInteractionEnabled) {
                                                        currentOnDragStart()
                                                    }
                                                }
                                                dragChange.consume()
                                                if (currentInteractionEnabled) {
                                                    currentOnDrag(dragAmount)
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (currentInteractionEnabled) {
                                        if (isDragging) {
                                            val dropCenter = Offset(
                                                cardPositionInRoot.x + currentDragOffset.x + (cardSize.width / 2),
                                                cardPositionInRoot.y + currentDragOffset.y + (cardSize.height / 2)
                                            )
                                            currentOnDragEnd(dropCenter)
                                        } else {
                                            // Simple click/tap - do not select or float card!
                                            currentOnTap()
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (currentInteractionEnabled && isDragging) {
                                        currentOnDragCancel()
                                    }
                                }
                            }
                        }
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .size(width = 85.dp, height = 110.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isFaceUp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    val hasSpace = card.text.contains(" ")
                    val longestSubword = if (hasSpace) {
                        card.text.split(" ").maxOfOrNull { it.length } ?: card.text.length
                    } else {
                        card.text.length
                    }

                    val dynamicFontSize = when {
                        longestSubword >= 12 -> 11.sp
                        longestSubword >= 10 -> 12.5.sp
                        longestSubword >= 8 -> 14.sp
                        longestSubword >= 6 -> 15.5.sp
                        else -> 17.sp
                    }
                    val dynamicLetterSpacing = when {
                        longestSubword >= 11 -> (-0.5).sp
                        longestSubword >= 9 -> (-0.2).sp
                        else -> 0.sp
                    }
                    val maxLinesCount = if (hasSpace) 2 else 1

                    if (isJoker) {
                        Text(
                            text = "🃏 JOKER",
                            color = Color(0xFF6B21A8),
                            fontFamily = nunitoFont,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.W800,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "HER KELİME",
                            color = Color(0xFF991B1B),
                            fontFamily = nunitoFont,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.W800,
                            textAlign = TextAlign.Center
                        )
                    } else if (card.isCategory) {
                        Text(
                            text = "👑 KAT",
                            color = AccentGold,
                            fontFamily = nunitoFont,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.W800,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = card.text,
                            color = Color.Black,
                            fontFamily = nunitoFont,
                            fontSize = dynamicFontSize,
                            fontWeight = FontWeight.W800,
                            maxLines = maxLinesCount,
                            letterSpacing = dynamicLetterSpacing,
                            textAlign = TextAlign.Center,
                            lineHeight = (dynamicFontSize.value * 1.15f).sp
                        )
                    } else {
                        Text(
                            text = card.text,
                            color = Color.Black,
                            fontFamily = nunitoFont,
                            fontSize = dynamicFontSize,
                            fontWeight = FontWeight.W800,
                            maxLines = maxLinesCount,
                            letterSpacing = dynamicLetterSpacing,
                            textAlign = TextAlign.Center,
                            lineHeight = (dynamicFontSize.value * 1.15f).sp
                        )
                    }
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFF1976D2))
                    
                    val cardWidth = size.width
                    val cardHeight = size.height
                    val step = 15f
                    
                    val path = Path()
                    for (x in -cardHeight.toInt()..cardWidth.toInt() step step.toInt()) {
                        path.moveTo(x.toFloat(), 0f)
                        path.lineTo(x.toFloat() + cardHeight, cardHeight)
                    }
                    for (x in 0..(cardWidth.toInt() + cardHeight.toInt()) step step.toInt()) {
                        path.moveTo(x.toFloat(), 0f)
                        path.lineTo(x.toFloat() - cardHeight, cardHeight)
                    }
                    
                    drawPath(
                        path = path,
                        color = Color(0xFF42A5F5),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}
