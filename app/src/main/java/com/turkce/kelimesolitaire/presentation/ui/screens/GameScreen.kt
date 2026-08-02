package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import com.turkce.kelimesolitaire.data.model.LevelData
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.presentation.ui.components.CategoryDropZone
import com.turkce.kelimesolitaire.presentation.ui.components.WordCard
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun GameScreen(
    levelData: LevelData,
    foundationSlots: List<FoundationSlot>,
    tableauPiles: List<List<SolitaireCard>>,
    stockPile: List<SolitaireCard>,
    wastePile: List<SolitaireCard>,
    totalWordsToMatch: Int,
    selectedCardId: String?,
    shakingCardId: String?,
    score: Int,
    coins: Int,
    completedLevels: Set<Int>,
    movesRemaining: Int,
    errors: Int,
    hintedCardId: String?,
    hintedTargetId: String?,
    showOutofMovesDialog: Boolean,
    completedCategoryName: String?,
    onCardSelected: (String?) -> Unit,
    onCardDropped: (List<SolitaireCard>, FoundationSlot) -> Boolean,
    onCardStacked: (List<SolitaireCard>, Int) -> Boolean,
    onDrawFromStock: () -> Unit,
    onRestartLevel: () -> Unit,
    onBackToMenu: () -> Unit,
    onShowHint: () -> Unit,
    onUndoLastMove: () -> Unit,
    onUseJoker: () -> Unit,
    onBuyExtraMoves: () -> Unit,
    onAcceptDefeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnimatingReturn by remember { mutableStateOf(false) }

    // Moves counter animated decrement flash and scale
    var prevMoves by remember { mutableStateOf(movesRemaining) }
    var triggerFlash by remember { mutableStateOf(false) }

    LaunchedEffect(movesRemaining) {
        if (movesRemaining < prevMoves) {
            triggerFlash = true
        }
        prevMoves = movesRemaining
    }

    val movesScale by animateFloatAsState(
        targetValue = if (triggerFlash) 1.25f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        finishedListener = { triggerFlash = false }
    )

    val movesColor by animateColorAsState(
        targetValue = if (triggerFlash) Color(0xFFE74C3C) else Color.White,
        animationSpec = tween(durationMillis = 200)
    )
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirmDialog = true
    }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Bouncy scale animation for Coins HUD bubble
    val coinScale = remember { Animatable(1f) }
    LaunchedEffect(coins) {
        if (coins > 0) { // Animate on coin changes
            coinScale.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            coinScale.animateTo(1f, animationSpec = tween(durationMillis = 150))
        }
    }

    val isReplay = remember(levelData.levelNumber, completedLevels) {
        completedLevels.contains(levelData.levelNumber)
    }

    // List of floating coins text (+2) to animate
    var floatingCoins by remember { mutableStateOf<List<FloatingCoinText>>(emptyList()) }
    
    // Group dragging states
    var draggedCards by remember { mutableStateOf<List<SolitaireCard>>(emptyList()) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Bounding boxes of Category Foundation slots
    val dropZoneBounds = remember { mutableStateMapOf<String, Rect>() }
    // Bounding boxes of bottom cards (or empty containers) in Tableau columns
    val tableauBounds = remember { mutableStateMapOf<Int, Rect>() }

    // Detect if user is dragging a waste pile card (need higher z-index overlay!)
    val isWasteDragging = wastePile.lastOrNull()?.let { top -> draggedCards.any { it.id == top.id } } ?: false

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E5E3A), Color(0xFF0F3620))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // 1. TOP HEADER ROW (Menu, Level Title, Coins)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Coins (with bouncy pulse scale animation)
                Text(
                    text = "🪙 $coins",
                    color = AccentGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.scale(coinScale.value)
                )

                // Center: Level Title
                Text(
                    text = "Seviye ${levelData.levelNumber}",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                // Right: Controls Row (Restart & Hamburger Menu)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restart Level Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onRestartLevel() }
                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↺",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Hamburger Menu
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showExitConfirmDialog = true }
                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☰",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. HUD & PILES ROW (Moves, Progress Bar, and Stock/Waste piles)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .padding(horizontal = 16.dp)
                    .zIndex(if (isWasteDragging) 5f else 1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Moves Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(110.dp)
                        .height(68.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "KALAN HAMLE",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$movesRemaining",
                            color = movesColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.scale(movesScale)
                        )
                    }
                }

                // Right Row: Piles (Waste & Stock)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Waste card slot
                    val topWaste = wastePile.lastOrNull()
                    if (topWaste != null) {
                        key(topWaste.id) {
                            val isDragged = draggedCards.any { it.id == topWaste.id }
                            val isWasteHinted = hintedCardId == topWaste.id
                            WordCard(
                                card = topWaste,
                                isSelected = selectedCardId == topWaste.id || isDragged,
                                isShaking = shakingCardId == topWaste.id,
                                isDragged = isDragged,
                                dragOffset = if (isDragged) dragOffset else Offset.Zero,
                                isInteractionEnabled = !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
                                onTap = {
                                    if (selectedCardId == topWaste.id) onCardSelected(null)
                                    else onCardSelected(topWaste.id)
                                },
                                onDragStart = {
                                    draggedCards = listOf(topWaste)
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { dragAmount ->
                                    dragOffset = Offset(dragOffset.x + dragAmount.x, dragOffset.y + dragAmount.y)
                                },
                                onDragEnd = { dropCenter ->
                                    val finalGroup = draggedCards
                                    if (finalGroup.isNotEmpty()) {
                                        val matchedSlot = foundationSlots.find { slot ->
                                            val bounds = dropZoneBounds[slot.id.toString()]
                                            bounds != null && bounds.contains(dropCenter)
                                        }
                                        if (matchedSlot != null) {
                                            val success = onCardDropped(finalGroup, matchedSlot)
                                            if (success) {
                                                draggedCards = emptyList()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (!isReplay) {
                                                    val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                    if (bounds != null) {
                                                        val wordCount = finalGroup.count { !it.isCategory }
                                                        val earnedAmount = wordCount * 2
                                                        if (earnedAmount > 0) {
                                                            SoundEffects.playCoinSound()
                                                            floatingCoins = floatingCoins + FloatingCoinText(
                                                                id = System.currentTimeMillis() + matchedSlot.id,
                                                                text = "+$earnedAmount 🪙",
                                                                startOffset = Offset(bounds.left + (bounds.width / 2) - 40f, bounds.top - 20f)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    isAnimatingReturn = true
                                                    val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                    anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                        dragOffset = this.value
                                                    }
                                                    draggedCards = emptyList()
                                                    onCardSelected(null)
                                                    isAnimatingReturn = false
                                                }
                                            }
                                        } else {
                                            var matchedColIdx = -1
                                            for (cIdx in 0..3) {
                                                val bounds = tableauBounds[cIdx]
                                                if (bounds != null && bounds.contains(dropCenter)) {
                                                    matchedColIdx = cIdx
                                                    break
                                                }
                                            }
                                            if (matchedColIdx != -1) {
                                                val success = onCardStacked(finalGroup, matchedColIdx)
                                                if (success) {
                                                    draggedCards = emptyList()
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                } else {
                                                    coroutineScope.launch {
                                                        isAnimatingReturn = true
                                                        val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                        anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                            dragOffset = this.value
                                                        }
                                                        draggedCards = emptyList()
                                                        onCardSelected(null)
                                                        isAnimatingReturn = false
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    isAnimatingReturn = true
                                                    val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                    anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                        dragOffset = this.value
                                                    }
                                                    draggedCards = emptyList()
                                                    onCardSelected(null)
                                                    isAnimatingReturn = false
                                                }
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        isAnimatingReturn = true
                                        val anim = Animatable(dragOffset, Offset.VectorConverter)
                                        anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                            dragOffset = this.value
                                        }
                                        draggedCards = emptyList()
                                        isAnimatingReturn = false
                                    }
                                },
                                modifier = Modifier.then(
                                    if (isWasteHinted) Modifier.border(2.5.dp, Color(0xFFF1C40F), RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Boş", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)
                        }
                    }

                    // Stock card slot
                    val isStockHinted = hintedCardId == "stock_pile"
                    if (stockPile.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .clickable { onDrawFromStock() }
                                .then(
                                    if (isStockHinted) Modifier.border(2.5.dp, Color(0xFFF1C40F), RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                        ) {
                            WordCard(
                                card = SolitaireCard(
                                    id = "stock_back",
                                    text = "",
                                    categoryId = "",
                                    isCategory = false,
                                    isFaceUp = false
                                ),
                                isSelected = false,
                                isShaking = false,
                                isDragged = false,
                                dragOffset = Offset.Zero,
                                isInteractionEnabled = false,
                                onTap = {},
                                onDragStart = {},
                                onDrag = {},
                                onDragEnd = {},
                                onDragCancel = {}
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .border(
                                    width = if (isStockHinted) 2.5.dp else 1.5.dp,
                                    color = if (isStockHinted) Color(0xFFF1C40F) else AccentGold.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDrawFromStock() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "♻️\nYenile",
                                color = AccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. MAIN SOLITAIRE TABLE BOARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(if (!isWasteDragging && draggedCards.isNotEmpty()) 5f else 1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                for (colIdx in 0..3) {
                    val colList = tableauPiles[colIdx]
                    val isColDragging = draggedCards.isNotEmpty() && colList.any { c -> draggedCards.any { it.id == c.id } }
                    val isColHinted = hintedTargetId == "col_$colIdx"
                    
                    Column(
                        modifier = Modifier
                            .width(85.dp)
                            .fillMaxHeight()
                            .zIndex(if (isColDragging) 10f else 1f)
                            .then(
                                if (isColHinted) Modifier.border(2.dp, Color(0xFFF1C40F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Category Drop Zone Slot
                        val slot = foundationSlots[colIdx]
                        val totalWordsForSlot = if (slot.activeCategory != null) {
                            levelData.targetWords.count { it.categoryId == slot.activeCategory.id }
                        } else 0

                        val isSlotHinted = hintedTargetId == "slot_${slot.id}"

                        CategoryDropZone(
                            slot = slot,
                            totalWords = totalWordsForSlot,
                            isHighlighted = isSlotHinted || (selectedCardId != null && run {
                                val selected = (tableauPiles.flatten() + wastePile).find { it.id == selectedCardId }
                                selected != null && selected.categoryId == slot.activeCategory?.id
                            }),
                            onTap = {
                                selectedCardId?.let { cardId ->
                                    val cardFromWaste = wastePile.lastOrNull()?.takeIf { it.id == cardId }
                                    
                                    var cardFromTableau: SolitaireCard? = null
                                    var tabIdx = -1
                                    for (card in colList) {
                                        if (card.id == cardId && card.isFaceUp) {
                                            cardFromTableau = card
                                            tabIdx = colList.indexOf(card)
                                            break
                                        }
                                    }

                                    if (cardFromWaste != null) {
                                        onCardDropped(listOf(cardFromWaste), slot)
                                    } else if (cardFromTableau != null && tabIdx != -1) {
                                        val group = colList.subList(tabIdx, colList.size)
                                        onCardDropped(group, slot)
                                    }
                                }
                            },
                            onBoundsPositioned = { s, rect ->
                                dropZoneBounds[s.id.toString()] = rect
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cascading Tableau Stack
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .onGloballyPositioned { coordinates ->
                                    if (colList.isEmpty()) {
                                        tableauBounds[colIdx] = coordinates.boundsInRoot()
                                    }
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            colList.forEachIndexed { rowIdx, card ->
                                key(card.id) {
                                    val isDragged = draggedCards.any { it.id == card.id }
                                    val isCardHinted = hintedCardId == card.id
                                    
                                    WordCard(
                                        card = card,
                                        isSelected = selectedCardId == card.id || isDragged,
                                        isShaking = shakingCardId == card.id,
                                        isDragged = isDragged,
                                        dragOffset = if (isDragged) dragOffset else Offset.Zero,
                                        isInteractionEnabled = !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
                                        onTap = {
                                            if (selectedCardId == card.id) onCardSelected(null)
                                            else onCardSelected(card.id)
                                        },
                                        onDragStart = {
                                            val group = colList.subList(rowIdx, colList.size)
                                            draggedCards = group
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { dragAmount ->
                                            dragOffset = Offset(dragOffset.x + dragAmount.x, dragOffset.y + dragAmount.y)
                                        },
                                        onDragEnd = { dropCenter ->
                                            val finalGroup = draggedCards
                                            if (finalGroup.isNotEmpty()) {
                                                val matchedSlot = foundationSlots.find { slot ->
                                                    val bounds = dropZoneBounds[slot.id.toString()]
                                                    bounds != null && bounds.contains(dropCenter)
                                                }
                                                if (matchedSlot != null) {
                                                    val success = onCardDropped(finalGroup, matchedSlot)
                                                    if (success) {
                                                        draggedCards = emptyList()
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        if (!isReplay) {
                                                            val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                            if (bounds != null) {
                                                                val wordCount = finalGroup.count { !it.isCategory }
                                                                val earnedAmount = wordCount * 2
                                                                if (earnedAmount > 0) {
                                                                    SoundEffects.playCoinSound()
                                                                    floatingCoins = floatingCoins + FloatingCoinText(
                                                                        id = System.currentTimeMillis() + matchedSlot.id.hashCode(),
                                                                        text = "+$earnedAmount 🪙",
                                                                        startOffset = Offset(bounds.left + (bounds.width / 2) - 40f, bounds.top - 20f)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        coroutineScope.launch {
                                                            isAnimatingReturn = true
                                                            val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                            anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                                dragOffset = this.value
                                                            }
                                                            draggedCards = emptyList()
                                                            onCardSelected(null)
                                                            isAnimatingReturn = false
                                                        }
                                                    }
                                                } else {
                                                    var matchedColIdx = -1
                                                    for (cIdx in 0..3) {
                                                        val bounds = tableauBounds[cIdx]
                                                        if (bounds != null && bounds.contains(dropCenter)) {
                                                            matchedColIdx = cIdx
                                                            break
                                                        }
                                                    }
                                                    if (matchedColIdx != -1) {
                                                        val success = onCardStacked(finalGroup, matchedColIdx)
                                                        if (success) {
                                                            draggedCards = emptyList()
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        } else {
                                                            coroutineScope.launch {
                                                                isAnimatingReturn = true
                                                                val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                                anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                                    dragOffset = this.value
                                                                }
                                                                draggedCards = emptyList()
                                                                onCardSelected(null)
                                                                isAnimatingReturn = false
                                                            }
                                                        }
                                                    } else {
                                                        coroutineScope.launch {
                                                            isAnimatingReturn = true
                                                            val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                            anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                                dragOffset = this.value
                                                            }
                                                            draggedCards = emptyList()
                                                            onCardSelected(null)
                                                            isAnimatingReturn = false
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                isAnimatingReturn = true
                                                val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                    dragOffset = this.value
                                                }
                                                draggedCards = emptyList()
                                                isAnimatingReturn = false
                                            }
                                        },
                                        modifier = Modifier
                                            .offset(y = (rowIdx * 25).dp)
                                            .onGloballyPositioned { coordinates ->
                                                if (rowIdx == colList.size - 1) {
                                                    tableauBounds[colIdx] = coordinates.boundsInRoot()
                                                }
                                            }
                                            .then(
                                                if (isCardHinted) Modifier.border(2.5.dp, Color(0xFFF1C40F), RoundedCornerShape(12.dp))
                                                else Modifier
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. BOTTOM UTILITY TOOLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. HINT BUTTON
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onShowHint() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💡", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "50 🪙",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 2. UNDO BUTTON
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onUndoLastMove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "↩️", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "50 🪙",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 3. JOKER BUTTON
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onUseJoker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🃏", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "200 🪙",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 4. OUT OF MOVES DIALOG OVERLAY
        if (showOutofMovesDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(100f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3B24)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(300.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hamle Bitti!",
                            color = AccentGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Kelimeleri eşleştirmek için hamleniz kalmadı. Devam etmek için ek hamle alın veya yenilgiyi kabul edin.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Buy Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBuyExtraMoves() }
                                .background(Color(0xFFE5A93C), shape = RoundedCornerShape(8.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "5 Ek Hamle: 75 🪙",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Give up Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAcceptDefeat() }
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yenilgiyi Kabul Et",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. EXIT CONFIRMATION DIALOG OVERLAY
        if (showExitConfirmDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .zIndex(100f)
                    .clickable { showExitConfirmDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3B24)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(300.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oyundan Çık",
                            color = AccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Oyundan çıkmak istiyor musunuz? İlerlemeniz kaydedilecektir.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showExitConfirmDialog = false }
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "İptal",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        showExitConfirmDialog = false
                                        onBackToMenu() 
                                    }
                                    .background(Color(0xFFE5A93C), shape = RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Çıkış Yap",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. FLOATING COIN FEEDBACK OVERLAYS
        floatingCoins.forEach { item ->
            key(item.id) {
                val animY = remember { Animatable(0f) }
                val animAlpha = remember { Animatable(1f) }
                
                LaunchedEffect(Unit) {
                    launch {
                        animY.animateTo(-140f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                    }
                    launch {
                        animAlpha.animateTo(0f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                    }
                    delay(1500) // Wait for animations to complete before removing from list
                    floatingCoins = floatingCoins.filter { it.id != item.id }
                }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = item.startOffset.x.roundToInt(),
                                y = (item.startOffset.y + animY.value).roundToInt()
                            )
                        }
                        .graphicsLayer(
                            alpha = animAlpha.value,
                            scaleX = 1.1f,
                            scaleY = 1.1f
                        )
                ) {
                    Text(
                        text = item.text,
                        color = Color(0xFFF1C40F), // Bright Golden Yellow
                        fontSize = 22.sp, // Larger, more visible font
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(3f, 3f),
                                blurRadius = 6f
                            )
                        )
                    )
                }
            }

            // 5. CATEGORY COMPLETED CELEBRATION OVERLAY
            completedCategoryName?.let { categoryName ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .zIndex(150f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F3620), Color(0xFF071F11))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, AccentGold, RoundedCornerShape(16.dp))
                            .padding(24.dp)
                            .width(260.dp)
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "KATEGORİ TAMAMLANDI!",
                            color = AccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = categoryName.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Harika Eşleştirme! 🌟",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

data class FloatingCoinText(
    val id: Long,
    val text: String,
    val startOffset: Offset
)

object SoundEffects {
    fun playCoinSound() {
        try {
            Thread {
                try {
                    val sampleRate = 44100
                    val numSamples = (sampleRate * 0.20).toInt()
                    val sample = DoubleArray(numSamples)
                    val generatedSnd = ByteArray(2 * numSamples)

                    for (i in 0 until numSamples) {
                        val freq = if (i < sampleRate * 0.06) 987.77 else 1318.51
                        val fadePercent = if (i > sampleRate * 0.10) {
                            maxOf(0.0, 1.0 - ((i - sampleRate * 0.10) / (sampleRate * 0.10)))
                        } else {
                            1.0
                        }
                        sample[i] = Math.sin(2.0 * Math.PI * i / (sampleRate / freq)) * fadePercent * 0.4
                    }

                    var idx = 0
                    for (dVal in sample) {
                        val valShort = (dVal * 32767).toInt().toShort()
                        generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                        generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                    }

                    val audioTrack = android.media.AudioTrack.Builder()
                        .setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            android.media.AudioFormat.Builder()
                                .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(generatedSnd.size)
                        .setTransferMode(android.media.AudioTrack.MODE_STATIC)
                        .build()
                    audioTrack.write(generatedSnd, 0, generatedSnd.size)
                    audioTrack.play()
                    
                    Thread.sleep(220)
                    audioTrack.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
