package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import com.turkce.kelimesolitaire.data.model.LevelData
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.presentation.ui.components.CategoryDropZone
import com.turkce.kelimesolitaire.presentation.ui.components.WordCard
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
import kotlinx.coroutines.launch

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
    movesRemaining: Int,
    errors: Int,
    onCardSelected: (String?) -> Unit,
    onCardDropped: (List<SolitaireCard>, FoundationSlot) -> Boolean,
    onCardStacked: (List<SolitaireCard>, Int) -> Boolean,
    onDrawFromStock: () -> Unit,
    onRestartLevel: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnimatingReturn by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirmDialog = true
    }

    val coroutineScope = rememberCoroutineScope()
    
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                // Left: Coins
                Text(
                    text = "🪙 $coins",
                    color = AccentGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
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
                // Left Column: Moves and Star Progress Bar
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(160.dp)
                ) {
                    Text(
                        text = "Hamle: $movesRemaining",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val initialMoves = (totalWordsToMatch * 3) + 15
                    val progress = movesRemaining.toFloat() / initialMoves.toFloat()
                    
                    // Star Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Progress fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFE5A93C),
                                            PrimaryNeon
                                        )
                                    )
                                )
                                .align(Alignment.CenterStart)
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
                                }
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
                    if (stockPile.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .clickable { onDrawFromStock() }
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
                                    width = 1.5.dp,
                                    color = AccentGold.copy(alpha = 0.3f),
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
                    
                    Column(
                        modifier = Modifier
                            .width(85.dp)
                            .fillMaxHeight()
                            .zIndex(if (isColDragging) 10f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Category Drop Zone Slot
                        val slot = foundationSlots[colIdx]
                        val totalWordsForSlot = if (slot.activeCategory != null) {
                            levelData.targetWords.count { it.categoryId == slot.activeCategory.id }
                        } else 0

                        CategoryDropZone(
                            slot = slot,
                            totalWords = totalWordsForSlot,
                            isHighlighted = selectedCardId != null && run {
                                val selected = (tableauPiles.flatten() + wastePile).find { it.id == selectedCardId }
                                selected != null && selected.categoryId == slot.activeCategory?.id
                            },
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
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(BorderGlass)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💡", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(BorderGlass)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "↩️", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(BorderGlass)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🃏", fontSize = 16.sp)
                }
            }
        }

        // 4. EXIT CONFIRMATION DIALOG OVERLAY
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
    }
}
