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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import com.turkce.kelimesolitaire.R
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
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
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
    shatteringJokerId: String? = null,
    isAdFree: Boolean = false,
    onOpenStore: () -> Unit = {},
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
    onWatchAdForCoins: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val nunitoFont = rememberNunitoFont()
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
    var showHamburgerMenu by remember { mutableStateOf(false) }
    var isSoundEnabled by rememberSaveable { mutableStateOf(true) }
    var isHapticEnabled by rememberSaveable { mutableStateOf(true) }
    
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
                // Left: Coins Status Pill (matching reference design)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .scale(coinScale.value)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A364F).copy(alpha = 0.9f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .clickable { onOpenStore() }
                        .padding(start = 4.dp, end = 12.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 30.dp)
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = 2.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$coins",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }

                // Center: Level Title
                OutlinedText(
                    text = "SEVİYE ${levelData.levelNumber}",
                    textColor = TextPrimary,
                    outlineColor = Color(0xFF0F172A),
                    outlineWidth = 5f,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )

                // Right: Settings Icon (No border box)
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.setting),
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showHamburgerMenu = true }
                )
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$movesRemaining",
                            color = movesColor,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont,
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
                                isShattering = shatteringJokerId == topWaste.id,
                                isDragged = isDragged,
                                dragOffset = if (isDragged) dragOffset else Offset.Zero,
                                isInteractionEnabled = !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
                                onTap = {},
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
                                                if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (!isReplay) {
                                                    val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                    if (bounds != null) {
                                                        val wordCount = finalGroup.count { !it.isCategory }
                                                        val earnedAmount = wordCount * 2
                                                        if (earnedAmount > 0) {
                                                            if (isSoundEnabled) SoundEffects.playCoinSound()
                                                            floatingCoins = floatingCoins + FloatingCoinText(
                                                                id = System.currentTimeMillis() + matchedSlot.id,
                                                                text = "+$earnedAmount",
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
                                    .zIndex(if (isDragged) 100f else 0f)
                                    .then(
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont,
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
                            isHighlighted = isSlotHinted,
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
                            if (colList.isEmpty()) {
                                // Empty Column Card Slot Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 108.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .border(
                                            width = 1.5.dp,
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.35f),
                                                    Color.White.copy(alpha = 0.15f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedCardId?.let { cardId ->
                                                val cardFromWaste = wastePile.lastOrNull()?.takeIf { it.id == cardId }
                                                var cardFromTableau: SolitaireCard? = null
                                                var sourceColIdx = -1
                                                var sourceRowIdx = -1

                                                for (cI in 0..3) {
                                                    val cList = tableauPiles[cI]
                                                    val idx = cList.indexOfFirst { it.id == cardId && it.isFaceUp }
                                                    if (idx != -1) {
                                                        cardFromTableau = cList[idx]
                                                        sourceColIdx = cI
                                                        sourceRowIdx = idx
                                                        break
                                                    }
                                                }

                                                if (cardFromWaste != null) {
                                                    onCardStacked(listOf(cardFromWaste), colIdx)
                                                } else if (cardFromTableau != null && sourceColIdx != -1) {
                                                    val group = tableauPiles[sourceColIdx].subList(sourceRowIdx, tableauPiles[sourceColIdx].size)
                                                    onCardStacked(group, colIdx)
                                                }
                                            }
                                        }
                                )
                            }

                            colList.forEachIndexed { rowIdx, card ->
                                key(card.id) {
                                    val isDragged = draggedCards.any { it.id == card.id }
                                    val isCardHinted = hintedCardId == card.id
                                    
                                    WordCard(
                                        card = card,
                                        isSelected = selectedCardId == card.id || isDragged,
                                        isShaking = shakingCardId == card.id,
                                        isShattering = shatteringJokerId == card.id,
                                        isDragged = isDragged,
                                        dragOffset = if (isDragged) dragOffset else Offset.Zero,
                                        isInteractionEnabled = !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
                                        onTap = {},
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
                                                        if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        if (!isReplay) {
                                                            val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                            if (bounds != null) {
                                                                val wordCount = finalGroup.count { !it.isCategory }
                                                                val earnedAmount = wordCount * 2
                                                                if (earnedAmount > 0) {
                                                                    SoundEffects.playCoinSound()
                                                                    floatingCoins = floatingCoins + FloatingCoinText(
                                                                        id = System.currentTimeMillis() + matchedSlot.id.hashCode(),
                                                                        text = "+$earnedAmount",
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
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. HINT BUTTON (3D Tactile with Vector HintIcon & Top-Left Circular Coin Badge)
                Box(contentAlignment = Alignment.TopStart) {
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F2B1D))
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .size(68.dp)
                            .clickable { onShowHint() },
                        contentAlignment = Alignment.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.HintIcon(size = 38.dp)
                    }

                    // Top-Left Circular Coin Cost Badge
                    Box(
                        modifier = Modifier
                            .offset(x = (-8).dp, y = (-8).dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFD97706))
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 16.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "50",
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                }

                // 2. UNDO BUTTON (3D Tactile with Vector UndoIcon & Top-Left Circular Coin Badge)
                Box(contentAlignment = Alignment.TopStart) {
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F2B1D))
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .size(68.dp)
                            .clickable { onUndoLastMove() },
                        contentAlignment = Alignment.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.UndoIcon(size = 38.dp)
                    }

                    // Top-Left Circular Coin Cost Badge
                    Box(
                        modifier = Modifier
                            .offset(x = (-8).dp, y = (-8).dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFD97706))
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 16.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "50",
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                }

                // 3. JOKER BUTTON (3D Tactile Gold with Vector JokerIcon & Top-Left Circular Coin Badge)
                Box(contentAlignment = Alignment.TopStart) {
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFB8860B))
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            )
                            .size(68.dp)
                            .clickable { onUseJoker() },
                        contentAlignment = Alignment.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.JokerIcon(size = 38.dp)
                    }

                    // Top-Left Circular Coin Cost Badge
                    Box(
                        modifier = Modifier
                            .offset(x = (-8).dp, y = (-8).dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFD97706))
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 16.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "200",
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
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
                        OutlinedText(
                            text = "Hamle Bitti!",
                            textColor = AccentGold,
                            outlineColor = Color(0xFF0F172A),
                            outlineWidth = 4f,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Kelimeleri eşleştirmek için hamleniz kalmadı. Devam etmek için ek hamle alın veya yenilgiyi kabul edin.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
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
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
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
                                fontWeight = FontWeight.Bold,
                                fontFamily = nunitoFont
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
                        OutlinedText(
                            text = "Oyundan Çık",
                            textColor = AccentGold,
                            outlineColor = Color(0xFF0F172A),
                            outlineWidth = 4f,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Oyundan çıkmak istiyor musunuz? İlerlemeniz kaydedilecektir.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
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
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = nunitoFont
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
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. HAMBURGER MENU / SETTINGS OVERLAY DIALOG (MATCHING REFERENCE UI SCREENSHOT)
        if (showHamburgerMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(200f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showHamburgerMenu = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .shadow(20.dp, RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3826B4), Color(0xFF241584), Color(0xFF190D69))
                            )
                        )
                        .border(2.dp, Color(0xFF6366F1), RoundedCornerShape(26.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Arched Header Bar with Close (X) Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF4C38CE), Color(0xFF2C1990))
                                    )
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedText(
                                text = "Ayarlar",
                                textColor = Color.White,
                                outlineColor = Color(0xFF190D69),
                                outlineWidth = 5f,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            // Top Right Circular Close Button (cancel.png)
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.cancel),
                                contentDescription = "Close Settings",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(34.dp)
                                    .clickable { showHamburgerMenu = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Sound & Haptic Toggle Outer Container Box
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF150A54).copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Sound Speaker Toggle
                                Image(
                                    painter = painterResource(id = R.drawable.sound),
                                    contentDescription = "Sound",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { isSoundEnabled = !isSoundEnabled },
                                    colorFilter = if (!isSoundEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isSoundEnabled) 1f else 0.4f
                                )

                                // 2. Haptic Vibration Toggle
                                Image(
                                    painter = painterResource(id = R.drawable.vibrate),
                                    contentDescription = "Vibration",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { isHapticEnabled = !isHapticEnabled },
                                    colorFilter = if (!isHapticEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isHapticEnabled) 1f else 0.4f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Capsule Pills Column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Restart Level Pill (Teal/Cyan 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E))
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showHamburgerMenu = false
                                        onRestartLevel()
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.restart),
                                        contentDescription = "Restart",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Seviyeyi Restart Et",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 2. Privacy Policy Pill (Teal 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showHamburgerMenu = false
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.privacy_policy_url)))
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.shield),
                                        contentDescription = "Privacy Policy",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Gizlilik Politikası",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 3. Open Store Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showHamburgerMenu = false
                                        onOpenStore()
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.buy),
                                        contentDescription = "Store",
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Mağaza",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 4. Exit to Main Menu Pill (Red 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFDC2626))
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showHamburgerMenu = false
                                        onBackToMenu()
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.exit),
                                        contentDescription = "Exit to Menu",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ana Menüye Dön",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 22.dp)
                    }
                }
            }
        }

        // 5. CATEGORY COMPLETED CELEBRATION FLOATING BANNER (Non-blocking!)
        completedCategoryName?.let { categoryName ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp)
                    .zIndex(150f),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                            )
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "KATEGORİ TAMAMLANDI!",
                                color = AccentGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = categoryName.uppercase(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
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
