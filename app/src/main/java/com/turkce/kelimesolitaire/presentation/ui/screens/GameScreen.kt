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
    onCardDropped: (List<SolitaireCard>, FoundationSlot) -> Unit, // List signature for group matching
    onCardStacked: (List<SolitaireCard>, Int) -> Unit, // List signature for group column transfer
    onDrawFromStock: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToMenu()
    }

    val coroutineScope = rememberCoroutineScope()
    
    // Group dragging states
    var draggedCards by remember { mutableStateOf<List<SolitaireCard>>(emptyList()) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Bounding boxes of Category Foundation slots
    val dropZoneBounds = remember { mutableStateMapOf<String, Rect>() }
    // Bounding boxes of bottom cards (or empty containers) in Tableau columns
    val tableauBounds = remember { mutableStateMapOf<Int, Rect>() }

    val isWasteDragging = draggedCards.isNotEmpty() && wastePile.any { it.id == draggedCards.first().id }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E5E3A), // Solitaire Felt Green
                        Color(0xFF0F3621)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // 1. TOP ROW: Moves banner, HUD controls, and Stock/Waste piles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .zIndex(if (isWasteDragging) 5f else 1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left HUD Box: Back to Menu
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { onBackToMenu() }
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀ Menü",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center Title & Stats HUD
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.width(180.dp)
                ) {
                    Text(
                        text = "Seviye ${levelData.levelNumber}",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🪙 $coins  ⭐ $score",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
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
                        
                        // Remaining Moves Overlay Text (Centered inside the progress bar!)
                        Text(
                            text = "$movesRemaining Hamle",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.zIndex(2f) // Drawn on top of the progress fill!
                        )

                        // Symmetrical Star markers overlay
                        // Star 1 (at 2%)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 4.dp)
                                .zIndex(3f)
                        ) {
                            Text(
                                text = "★",
                                color = AccentGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Star 2 (at 15%)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = (180 * 0.15f).dp - 4.dp)
                                .zIndex(3f)
                        ) {
                            Text(
                                text = "★",
                                color = if (progress >= 0.15f) AccentGold else Color.Gray.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Star 3 (at 40%)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = (180 * 0.40f).dp - 4.dp)
                                .zIndex(3f)
                        ) {
                            Text(
                                text = "★",
                                color = if (progress >= 0.40f) AccentGold else Color.Gray.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Piles
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
                                isInteractionEnabled = draggedCards.isEmpty() || isDragged,
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
                                            draggedCards = emptyList()
                                            onCardDropped(finalGroup, matchedSlot)
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
                                                draggedCards = emptyList()
                                                onCardStacked(finalGroup, matchedColIdx)
                                            } else {
                                                coroutineScope.launch {
                                                    val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                    anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                        dragOffset = this.value
                                                    }
                                                    draggedCards = emptyList()
                                                    onCardSelected(null)
                                                }
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    draggedCards = emptyList()
                                    coroutineScope.launch {
                                        val anim = Animatable(dragOffset, Offset.VectorConverter)
                                        anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                            dragOffset = this.value
                                        }
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
                            Text(
                                text = "Boş",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 10.sp
                            )
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

                            // Remaining Card Count Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .background(Color.White, shape = CircleShape)
                                    .border(1.dp, Color(0xFF1976D2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${stockPile.size}",
                                    color = Color(0xFF1976D2),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Recycle pile slot
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

            // 2. MAIN SOLITAIRE TABLE BOARD (4 Vertical Lanes containing Category & Tableau Column)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(if (!isWasteDragging && draggedCards.isNotEmpty()) 5f else 1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                for (colIdx in 0..3) {
                    val slot = foundationSlots[colIdx]
                    val colList = tableauPiles[colIdx]
                    val isColDragging = draggedCards.isNotEmpty() && colList.any { it.id == draggedCards.first().id }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(85.dp)
                            .fillMaxHeight()
                            .zIndex(if (isColDragging) 5f else 1f)
                    ) {
                        // Category slot
                        val activeCategory = slot.activeCategory
                        val totalWordsForSlot = if (activeCategory != null) {
                            levelData.targetWords.count { it.categoryId == activeCategory.id }
                        } else 0

                        val isHighlighted = selectedCardId != null

                        CategoryDropZone(
                            slot = slot,
                            totalWords = totalWordsForSlot,
                            isHighlighted = isHighlighted,
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
                                    val isLast = rowIdx == colList.size - 1
                                    
                                    WordCard(
                                        card = card,
                                        isSelected = selectedCardId == card.id || isDragged,
                                        isShaking = shakingCardId == card.id,
                                        isDragged = isDragged,
                                        dragOffset = if (isDragged) dragOffset else Offset.Zero,
                                        isInteractionEnabled = draggedCards.isEmpty() || isDragged,
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
                                            android.util.Log.d("SolitaireDebug", "Tableau DragEnd: card=${card.text}, groupSize=${finalGroup.size}, dropCenter=$dropCenter")
                                            dropZoneBounds.forEach { (key, rect) ->
                                                android.util.Log.d("SolitaireDebug", "  DropZone $key: $rect, contains=${rect.contains(dropCenter)}")
                                            }
                                            if (finalGroup.isNotEmpty()) {
                                                val matchedSlot = foundationSlots.find { slot ->
                                                    val bounds = dropZoneBounds[slot.id.toString()]
                                                    bounds != null && bounds.contains(dropCenter)
                                                }
                                                if (matchedSlot != null) {
                                                    draggedCards = emptyList()
                                                    onCardDropped(finalGroup, matchedSlot)
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
                                                        draggedCards = emptyList()
                                                        onCardStacked(finalGroup, matchedColIdx)
                                                    } else {
                                                        coroutineScope.launch {
                                                            val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                            anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                                dragOffset = this.value
                                                            }
                                                            draggedCards = emptyList()
                                                            onCardSelected(null)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                val anim = Animatable(dragOffset, Offset.VectorConverter)
                                                anim.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessMedium)) {
                                                    dragOffset = this.value
                                                }
                                                draggedCards = emptyList()
                                            }
                                        },
                                        modifier = Modifier
                                            .offset(y = (rowIdx * 25).dp)
                                            .then(
                                                if (isLast) {
                                                    Modifier.onGloballyPositioned { coordinates ->
                                                        tableauBounds[colIdx] = coordinates.boundsInRoot()
                                                    }
                                                } else Modifier
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
    }
}
