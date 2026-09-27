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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
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
import com.turkce.kelimesolitaire.presentation.ui.components.MessageType
import com.turkce.kelimesolitaire.presentation.ui.components.Booster3DButton
import com.turkce.kelimesolitaire.presentation.ui.components.BoosterType
import com.turkce.kelimesolitaire.presentation.ui.components.HintIcon
import com.turkce.kelimesolitaire.presentation.ui.components.UndoIcon
import com.turkce.kelimesolitaire.presentation.ui.components.JokerIcon
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.GameTableBg
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackBg
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleBg
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleText
import com.turkce.kelimesolitaire.presentation.ui.theme.LevelTitleText
import com.turkce.kelimesolitaire.presentation.ui.theme.LevelTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.CoinBoxBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.CoinTextColor
import com.turkce.kelimesolitaire.presentation.ui.theme.CoinTextOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCardBg
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCardBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesTitleText
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCountText
import kotlinx.coroutines.launch

private fun findBestFoundationSlot(
    foundationSlots: List<FoundationSlot>,
    dropZoneBounds: Map<String, Rect>,
    testPoints: List<Offset>
): FoundationSlot? {
    return foundationSlots
        .mapNotNull { slot ->
            val bounds = dropZoneBounds[slot.id.toString()] ?: return@mapNotNull null
            val inflated = bounds.inflate(65f)
            val matchedPoint = testPoints.find { inflated.contains(it) }
            if (matchedPoint != null) {
                val dx = bounds.center.x - matchedPoint.x
                val dy = bounds.center.y - matchedPoint.y
                Pair(slot, dx * dx + dy * dy)
            } else null
        }
        .minByOrNull { it.second }
        ?.first
}

private fun findBestTableauColumn(
    tableauBounds: Map<Int, Rect>,
    testPoints: List<Offset>
): Int {
    return (0..3)
        .mapNotNull { cIdx ->
            val bounds = tableauBounds[cIdx] ?: return@mapNotNull null
            val inflated = Rect(
                left = bounds.left - 35f,
                top = bounds.top - 40f,
                right = bounds.right + 35f,
                bottom = maxOf(bounds.bottom + 220f, bounds.top + 600f)
            )
            val matchedPoint = testPoints.find { inflated.contains(it) }
            if (matchedPoint != null) {
                val dx = bounds.center.x - matchedPoint.x
                val dy = bounds.center.y - matchedPoint.y
                val dist = dx * dx + (dy * dy * 0.35f)
                Pair(cIdx, dist)
            } else null
        }
        .minByOrNull { it.second }
        ?.first ?: -1
}

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
    isLevelWon: Boolean = false,
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
    onShowMessage: (String, MessageType) -> Unit = { _, _ -> },
    isUndoUnlocked: Boolean = false,
    isHintUnlocked: Boolean = false,
    isJokerUnlocked: Boolean = false,
    hasFreeUndo: Boolean = false,
    hasFreeHint: Boolean = false,
    hasFreeJoker: Boolean = false,
    freeUndoCount: Int = if (hasFreeUndo) 1 else 0,
    freeHintCount: Int = if (hasFreeHint) 1 else 0,
    freeJokerCount: Int = if (hasFreeJoker) 1 else 0,
    activeTutorial: com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType? = null,
    level1TutorialStep: Int = 0,
    onDismissTutorial: () -> Unit = {},
    onDismissCategoryCelebration: () -> Unit = {},
    shouldAnimateDeal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()
    val density = LocalDensity.current
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
        targetValue = if (triggerFlash) Color(0xFFE74C3C) else MovesCountText,
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
    var isSoundEnabled by rememberSaveable { mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isSoundEnabled(context)) }
    var isHapticEnabled by rememberSaveable { mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isHapticEnabled(context)) }
    
    // Group dragging states
    var draggedCards by remember { mutableStateOf<List<SolitaireCard>>(emptyList()) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Dynamic stack compression animation during drag
    // Compresses stack vertical spacing from 25.dp (~15mm) down to 7.dp (~2mm visible edge)
    val isCompressingStack = draggedCards.size > 1 && !isAnimatingReturn
    val compressionFraction by animateFloatAsState(
        targetValue = if (isCompressingStack) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "stackCompression"
    )

    // Bounding boxes of Category Foundation slots
    val dropZoneBounds = remember { mutableStateMapOf<String, Rect>() }
    // Bounding boxes of bottom cards (or empty containers) in Tableau columns
    val tableauBounds = remember { mutableStateMapOf<Int, Rect>() }

    // Detect if user is dragging a waste pile card (need higher z-index overlay!)
    val isWasteDragging = wastePile.lastOrNull()?.let { top -> draggedCards.any { it.id == top.id } } ?: false

    // Card Dealing Animation state at level start / restart (bypassed if entering in-progress game)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var isDealingFinished by remember(levelData.levelNumber, shouldAnimateDeal) { mutableStateOf(!shouldAnimateDeal) }
    val dealAnimProgress = remember(levelData.levelNumber, shouldAnimateDeal) { Animatable(if (shouldAnimateDeal) 0f else 1f) }

    LaunchedEffect(levelData.levelNumber, shouldAnimateDeal) {
        if (!shouldAnimateDeal) {
            isDealingFinished = true
            dealAnimProgress.snapTo(1f)
            return@LaunchedEffect
        }
        dealAnimProgress.snapTo(0f)
        isDealingFinished = false
        // Smooth, clearly visible card deal flight over 1300ms
        launch {
            dealAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1300, easing = LinearEasing)
            )
            isDealingFinished = true
        }
        // Rapid rhythmic card snap audio during deal
        if (isSoundEnabled) {
            val totalCards = tableauPiles.sumOf { it.size }
            val soundCount = minOf(totalCards, 10)
            for (i in 0 until soundCount) {
                delay(95L)
                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(context)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameTableBg)
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
                // Left: Coins Status Box (With border, no background)
                Box(
                    modifier = Modifier
                        .scale(coinScale.value)
                        .clickable { onOpenStore() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Box border extending rightward from behind the coin (transparent background)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 18.dp)
                            .height(28.dp)
                            .border(
                                1.5.dp,
                                CoinBoxBorder,
                                RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp, topStart = 4.dp, bottomStart = 4.dp)
                            )
                            .padding(start = 22.dp, end = 12.dp)
                    ) {
                        Text(
                            text = LocaleHelper.formatNumber(coins, isPersian),
                            color = CoinTextColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }

                    // 3D Coin Icon on the outside, overlapping on the left
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .zIndex(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 38.dp)
                    }
                }

                // Center: Level Title
                OutlinedText(
                    text = LocaleHelper.levelTitle(levelData.levelNumber, isPersian),
                    textColor = LevelTitleText,
                    outlineColor = LevelTitleOutline,
                    outlineWidth = 10f,
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

            // LEVEL 1 INTERACTIVE TUTORIAL BANNER
            if (activeTutorial == com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType.LEVEL_1_GUIDE) {
                com.turkce.kelimesolitaire.presentation.ui.components.Level1InteractiveBanner(
                    step = level1TutorialStep,
                    isPersian = isPersian,
                    fontFamily = nunitoFont,
                    onSkip = onDismissTutorial
                )
            }

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
                    colors = CardDefaults.cardColors(containerColor = MovesCardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(110.dp)
                        .height(68.dp)
                        .border(1.dp, MovesCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = LocaleHelper.remainingMovesTitle(isPersian),
                            color = MovesTitleText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LocaleHelper.formatNumber(movesRemaining, isPersian),
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
                            val isWasteTutorialHinted = (levelData.levelNumber == 1 && level1TutorialStep == 0 && topWaste.isCategory) ||
                                    (levelData.levelNumber == 1 && level1TutorialStep == 1 && !topWaste.isCategory && foundationSlots.any { it.activeCategory != null && it.activeCategory.id == topWaste.categoryId })
                            val isWasteHinted = hintedCardId == topWaste.id || isWasteTutorialHinted
                            WordCard(
                                card = topWaste,
                                isSelected = selectedCardId == topWaste.id || isDragged,
                                isShaking = shakingCardId == topWaste.id,
                                isShattering = shatteringJokerId == topWaste.id,
                                isHinted = isWasteHinted,
                                isDragged = isDragged,
                                dragOffsetProvider = { dragOffset },
                                isInteractionEnabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog && !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
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
                                        val testPoints = if (finalGroup.size > 1) {
                                            listOf(
                                                dropCenter,
                                                Offset(dropCenter.x, dropCenter.y - ((finalGroup.size - 1) * 35f))
                                            )
                                        } else {
                                            listOf(dropCenter)
                                        }
                                        val matchedSlot = findBestFoundationSlot(foundationSlots, dropZoneBounds, testPoints)
                                        if (matchedSlot != null) {
                                            val success = onCardDropped(finalGroup, matchedSlot)
                                            if (success) {
                                                draggedCards = emptyList()
                                                if (isHapticEnabled) {
                                                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(context)
                                                }
                                                if (isSoundEnabled) {
                                                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(context)
                                                }
                                                if (!isReplay) {
                                                    val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                    if (bounds != null) {
                                                        val wordCount = finalGroup.count { !it.isCategory }
                                                        val earnedAmount = wordCount * 2
                                                        if (earnedAmount > 0) {
                                                            if (isSoundEnabled) {
                                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCoinSound(context)
                                                            }
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
                                            val matchedColIdx = findBestTableauColumn(tableauBounds, testPoints)
                                            if (matchedColIdx != -1) {
                                                val success = onCardStacked(finalGroup, matchedColIdx)
                                                if (success) {
                                                    draggedCards = emptyList()
                                                    if (isHapticEnabled) {
                                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(context)
                                                    }
                                                    if (isSoundEnabled) {
                                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(context)
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
                            Text(text = LocaleHelper.emptySlot(isPersian), color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)
                        }
                    }

                    // Stock card slot
                    val isStockHinted = hintedCardId == "stock_pile" || (levelData.levelNumber == 1 && level1TutorialStep == 2)
                    if (stockPile.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .clickable(enabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog) { onDrawFromStock() },
                            contentAlignment = Alignment.TopStart
                        ) {
                            // Layer 2 (Bottom layer for thick deck appearance if 3 or more cards)
                            if (stockPile.size >= 3) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 3.dp, y = 3.dp)
                                        .size(width = 85.dp, height = 110.dp)
                                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardBackBg.copy(alpha = 0.75f))
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                )
                            }
                            // Layer 1 (Middle layer if 2 or more cards)
                            if (stockPile.size >= 2) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 1.5.dp, y = 1.5.dp)
                                        .size(width = 85.dp, height = 110.dp)
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardBackBg)
                                        .border(1.2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                )
                            }
                            // Top Layer (WordCard)
                            Box(
                                modifier = Modifier
                                    .size(width = 85.dp, height = 110.dp)
                                    .then(
                                        if (isStockHinted) Modifier.border(2.5.dp, Color(0xFFF1C40F), RoundedCornerShape(8.dp))
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
                                    isHinted = isStockHinted,
                                    isDragged = false,
                                    dragOffsetProvider = { Offset.Zero },
                                    isInteractionEnabled = false,
                                    onTap = {},
                                    onDragStart = {},
                                    onDrag = {},
                                    onDragEnd = {},
                                    onDragCancel = {}
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StockRecycleBg)
                                .border(
                                    width = if (isStockHinted) 2.5.dp else 1.5.dp,
                                    color = if (isStockHinted) Color(0xFFF1C40F) else StockRecycleBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog) { onDrawFromStock() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPersian) "♻️\nبر زدن" else "♻️\nYenile",
                                color = StockRecycleText,
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

                        val isSlotTutorialHinted = (levelData.levelNumber == 1 && level1TutorialStep == 0 && slot.activeCategory == null && colIdx == 0) ||
                                (levelData.levelNumber == 1 && level1TutorialStep == 1 && slot.activeCategory != null && slot.matchedWords.size < totalWordsForSlot)
                        val isSlotHinted = hintedTargetId == "slot_${slot.id}" || isSlotTutorialHinted

                        CategoryDropZone(
                            slot = slot,
                            totalWords = totalWordsForSlot,
                            isHighlighted = isSlotHinted,
                            onTap = {
                                if (!isDealingFinished) return@CategoryDropZone
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

                                    val targetCard = cardFromTableau
                                    if (cardFromWaste != null) {
                                        onCardDropped(listOf(cardFromWaste), slot)
                                    } else if (targetCard != null && sourceColIdx != -1) {
                                        val sourceList = tableauPiles[sourceColIdx]
                                        val targetCatId = targetCard.categoryId
                                        var startIdx = sourceRowIdx
                                        while (startIdx > 0) {
                                            val prevCard = sourceList[startIdx - 1]
                                            if (!prevCard.isFaceUp) break
                                            val prevCatId = prevCard.categoryId
                                            val matches = prevCatId == targetCatId ||
                                                    prevCatId == "joker_wildcard" ||
                                                    targetCatId == "joker_wildcard"
                                            if (matches) {
                                                startIdx--
                                            } else {
                                                break
                                            }
                                        }
                                        val group = sourceList.subList(startIdx, sourceList.size)
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
                                    val boxBounds = coordinates.boundsInRoot()
                                    val existing = tableauBounds[colIdx]
                                    tableauBounds[colIdx] = if (existing != null) {
                                        Rect(
                                            left = boxBounds.left,
                                            top = boxBounds.top,
                                            right = boxBounds.right,
                                            bottom = maxOf(boxBounds.bottom, existing.bottom)
                                        )
                                    } else {
                                        boxBounds
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
                                        .clickable(enabled = isDealingFinished) {
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

                                                val targetCard = cardFromTableau
                                                if (cardFromWaste != null) {
                                                    onCardStacked(listOf(cardFromWaste), colIdx)
                                                } else if (targetCard != null && sourceColIdx != -1) {
                                                    val sourceList = tableauPiles[sourceColIdx]
                                                    val targetCatId = targetCard.categoryId
                                                    var startIdx = sourceRowIdx
                                                    while (startIdx > 0) {
                                                        val prevCard = sourceList[startIdx - 1]
                                                        if (!prevCard.isFaceUp) break
                                                        val prevCatId = prevCard.categoryId
                                                        val matches = prevCatId == targetCatId ||
                                                                prevCatId == "joker_wildcard" ||
                                                                targetCatId == "joker_wildcard"
                                                        if (matches) {
                                                            startIdx--
                                                        } else {
                                                            break
                                                        }
                                                    }
                                                    val group = sourceList.subList(startIdx, sourceList.size)
                                                    onCardStacked(group, colIdx)
                                                }
                                            }
                                        }
                                )
                            }

                            colList.forEachIndexed { rowIdx, card ->
                                key(card.id) {
                                    val dealIndex = rowIdx * 4 + colIdx
                                    val totalTableauCards = remember(tableauPiles) {
                                        tableauPiles.mapIndexed { cIdx, list -> list.indices.map { rIdx -> rIdx * 4 + cIdx } }.flatten().maxOrNull() ?: 1
                                    }
                                    val cardStart = if (totalTableauCards > 0) (dealIndex.toFloat() / (totalTableauCards + 2.5f)) * 0.56f else 0f
                                    val cardDuration = 0.44f
                                    val cardProgress = if (isDealingFinished) 1f else ((dealAnimProgress.value - cardStart) / cardDuration).coerceIn(0f, 1f)
                                    val eased = FastOutSlowInEasing.transform(cardProgress)

                                    val currentAlpha = if (isDealingFinished) 1f else if (cardProgress <= 0f) 0f else minOf(1f, cardProgress * 3.5f)
                                    val dirMultiplier = if (isRtl) -1f else 1f
                                    val startOffsetX = dirMultiplier * ((3 - colIdx) * 88f + 16f)
                                    val startOffsetY = -(210f + rowIdx * 25f)
                                    val arcLiftY = if (isDealingFinished) 0.dp else (kotlin.math.sin(cardProgress * Math.PI).toFloat() * -24f).dp
                                    val currentDealX = if (isDealingFinished) 0.dp else (startOffsetX * (1f - eased)).dp
                                    val currentDealY = if (isDealingFinished) (rowIdx * 25).dp else (((rowIdx * 25f) + startOffsetY * (1f - eased)).dp + arcLiftY)
                                    val startRotation = dirMultiplier * (if (colIdx < 2) (-14f + colIdx * 4f) else (6f + (colIdx - 2) * 5f))
                                    val currentRotation = if (isDealingFinished) 0f else (startRotation * (1f - eased))
                                    val currentScale = if (isDealingFinished) 1f else (0.80f + (0.20f * eased))

                                    val isDragged = draggedCards.any { it.id == card.id }
                                    val dragGroupIdx = if (isDragged) draggedCards.indexOfFirst { it.id == card.id } else -1
                                    val compressionYOffset = if (dragGroupIdx > 0) (- (dragGroupIdx * 18f * compressionFraction)).dp else 0.dp
                                    val isCardTutorialHinted = (levelData.levelNumber == 1 && level1TutorialStep == 0 && card.isFaceUp && card.isCategory) ||
                                            (levelData.levelNumber == 1 && level1TutorialStep == 1 && card.isFaceUp && !card.isCategory && foundationSlots.any { it.activeCategory != null && it.activeCategory.id == card.categoryId })
                                    val isCardHinted = hintedCardId == card.id || isCardTutorialHinted
                                    
                                    WordCard(
                                        card = card,
                                        isSelected = selectedCardId == card.id || isDragged,
                                        isShaking = shakingCardId == card.id,
                                        isShattering = shatteringJokerId == card.id,
                                        isHinted = isCardHinted,
                                        isDragged = isDragged,
                                        dragZIndex = if (dragGroupIdx >= 0) dragGroupIdx.toFloat() else 0f,
                                        dragOffsetProvider = { dragOffset },
                                        isInteractionEnabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog && !isAnimatingReturn && (draggedCards.isEmpty() || isDragged),
                                        onTap = {},
                                        onDragStart = {
                                            val targetCatId = card.categoryId
                                            var startIdx = rowIdx
                                            while (startIdx > 0) {
                                                val prevCard = colList[startIdx - 1]
                                                if (!prevCard.isFaceUp) break
                                                val prevCatId = prevCard.categoryId
                                                if (targetCatId != "joker_wildcard" && prevCatId == "joker_wildcard") {
                                                    break
                                                }
                                                val matches = prevCatId == targetCatId ||
                                                        prevCatId == "joker_wildcard" ||
                                                        targetCatId == "joker_wildcard"
                                                if (matches) {
                                                    startIdx--
                                                } else {
                                                    break
                                                }
                                            }
                                            val group = colList.subList(startIdx, colList.size)
                                            draggedCards = group
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { dragAmount ->
                                            dragOffset = Offset(dragOffset.x + dragAmount.x, dragOffset.y + dragAmount.y)
                                        },
                                        onDragEnd = { dropCenter ->
                                            val finalGroup = draggedCards
                                            if (finalGroup.isNotEmpty()) {
                                                val touchIndexInGroup = finalGroup.indexOfFirst { it.id == card.id }.coerceAtLeast(0)
                                                val normalStepPx = with(density) { 25.dp.toPx() }
                                                val compressedStepPx = with(density) { (25.dp - (18.dp * compressionFraction)).toPx() }
                                                val topCardCenter = Offset(
                                                    dropCenter.x,
                                                    dropCenter.y - (touchIndexInGroup * normalStepPx)
                                                )
                                                val bottomCardCenter = Offset(
                                                    dropCenter.x,
                                                    topCardCenter.y + ((finalGroup.size - 1) * compressedStepPx)
                                                )
                                                val midCardCenter = Offset(
                                                    dropCenter.x,
                                                    (topCardCenter.y + bottomCardCenter.y) / 2f
                                                )
                                                val testPoints = if (finalGroup.size > 1) {
                                                    listOf(dropCenter, topCardCenter, bottomCardCenter, midCardCenter)
                                                } else {
                                                    listOf(dropCenter)
                                                }
                                                val matchedSlot = findBestFoundationSlot(foundationSlots, dropZoneBounds, testPoints)
                                                if (matchedSlot != null) {
                                                    val success = onCardDropped(finalGroup, matchedSlot)
                                                    if (success) {
                                                        draggedCards = emptyList()
                                                        if (isHapticEnabled) {
                                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(context)
                                                        }
                                                        if (isSoundEnabled) {
                                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(context)
                                                        }
                                                        if (!isReplay) {
                                                            val bounds = dropZoneBounds[matchedSlot.id.toString()]
                                                            if (bounds != null) {
                                                                val wordCount = finalGroup.count { !it.isCategory }
                                                                val earnedAmount = wordCount * 2
                                                                if (earnedAmount > 0) {
                                                                    if (isSoundEnabled) {
                                                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCoinSound(context)
                                                                    }
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
                                                    val matchedColIdx = findBestTableauColumn(tableauBounds, testPoints)
                                                    if (matchedColIdx != -1) {
                                                        val success = onCardStacked(finalGroup, matchedColIdx)
                                                        if (success) {
                                                            draggedCards = emptyList()
                                                            if (isHapticEnabled) {
                                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(context)
                                                            }
                                                            if (isSoundEnabled) {
                                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(context)
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
                                            .offset(x = currentDealX, y = currentDealY + compressionYOffset)
                                            .graphicsLayer {
                                                rotationZ = currentRotation
                                                scaleX = currentScale
                                                scaleY = currentScale
                                                alpha = currentAlpha
                                            }
                                            .onGloballyPositioned { coordinates ->
                                                if (isDealingFinished && rowIdx == colList.size - 1 && draggedCards.isEmpty()) {
                                                    val cardBounds = coordinates.boundsInRoot()
                                                    val boxBounds = tableauBounds[colIdx]
                                                    tableauBounds[colIdx] = if (boxBounds != null) {
                                                        Rect(
                                                            left = boxBounds.left,
                                                            top = boxBounds.top,
                                                            right = boxBounds.right,
                                                            bottom = maxOf(boxBounds.bottom, cardBounds.bottom + 50f)
                                                        )
                                                    } else {
                                                        cardBounds
                                                    }
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. HINT BUTTON (Electric Cyan)
                    Booster3DButton(
                        type = BoosterType.HINT,
                        isUnlocked = isHintUnlocked,
                        lockText = if (isPersian) "🔒 مرحله ۸" else "🔒 8. Lvl",
                        hasFree = hasFreeHint,
                        freeCount = freeHintCount,
                        coinCost = 50,
                        isPersian = isPersian,
                        enabled = !isLevelWon && isDealingFinished,
                        fontFamily = nunitoFont,
                        onClick = {
                            if (!isHintUnlocked) {
                                onShowMessage(
                                    if (isPersian) "قابلیت راهنما در مرحله ۸ (سخت) باز می‌شود! 🔒" else "İpucu özelliği 8. seviyede açılır! 🔒",
                                    MessageType.WARNING
                                )
                            } else {
                                onShowHint()
                            }
                        },
                        icon = {
                            HintIcon(size = 56.dp)
                        }
                    )

                    // 2. UNDO BUTTON (Cosmic Royal Violet)
                    Booster3DButton(
                        type = BoosterType.UNDO,
                        isUnlocked = isUndoUnlocked,
                        lockText = if (isPersian) "🔒 مرحله ۲" else "🔒 2. Lvl",
                        hasFree = hasFreeUndo,
                        freeCount = freeUndoCount,
                        coinCost = 50,
                        isPersian = isPersian,
                        enabled = !isLevelWon && isDealingFinished,
                        fontFamily = nunitoFont,
                        onClick = {
                            if (!isUndoUnlocked) {
                                onShowMessage(
                                    if (isPersian) "قابلیت بازگشت در مرحله ۲ باز می‌شود! 🔒" else "Geri Al özelliği 2. seviyede açılır! 🔒",
                                    MessageType.WARNING
                                )
                            } else {
                                onUndoLastMove()
                            }
                        },
                        icon = {
                            UndoIcon(size = 48.dp)
                        }
                    )

                    // 3. JOKER BUTTON (24K Gold & Amber)
                    Booster3DButton(
                        type = BoosterType.JOKER,
                        isUnlocked = isJokerUnlocked,
                        lockText = if (isPersian) "🔒 مرحله ۱۰" else "🔒 10. Lvl",
                        hasFree = hasFreeJoker,
                        freeCount = freeJokerCount,
                        coinCost = 200,
                        isPersian = isPersian,
                        enabled = !isLevelWon && isDealingFinished,
                        fontFamily = nunitoFont,
                        onClick = {
                            if (!isJokerUnlocked) {
                                onShowMessage(
                                    if (isPersian) "کارت جوکر در مرحله ۱۰ (خیلی سخت) باز می‌شود! 🔒" else "Joker kartı 10. seviyede açılır! 🔒",
                                    MessageType.WARNING
                                )
                            } else {
                                onUseJoker()
                            }
                        },
                        icon = {
                            JokerIcon(size = 48.dp)
                        }
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
                        OutlinedText(
                            text = LocaleHelper.outOfMovesTitle(isPersian),
                            textColor = AccentGold,
                            outlineColor = Color(0xFF0F172A),
                            outlineWidth = 4f,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = LocaleHelper.outOfMovesPrompt(isPersian),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
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
                                text = if (isPersian) "+۵ حرکت: ۷۵ 🪙" else "5 Ek Hamle: 75 🪙",
                                color = Color.Black,
                                fontSize = 16.sp,
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
                                text = LocaleHelper.giveUp(isPersian),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                }
            }
        }

        // 5. EXIT CONFIRMATION DIALOG OVERLAY (MATCHING GAME DESIGN SYSTEM)
        if (showExitConfirmDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(250f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showExitConfirmDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .shadow(24.dp, RoundedCornerShape(26.dp))
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
                                text = LocaleHelper.exitDialogTitle(isPersian),
                                textColor = Color.White,
                                outlineColor = Color(0xFF190D69),
                                outlineWidth = 5f,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            // Top Right Circular Close Button (cancel.png)
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.cancel),
                                contentDescription = "Close",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(34.dp)
                                    .clickable { showExitConfirmDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Question Prompt & Subtitle
                        Text(
                            text = LocaleHelper.exitDialogPrompt(isPersian),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = LocaleHelper.exitDialogDesc(isPersian),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = nunitoFont,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Two Action Capsule Buttons Side by Side (Stay & Leave)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Leave to Menu (Red/Rose 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFFDA4AF).copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        showExitConfirmDialog = false
                                        onBackToMenu()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.exit),
                                        contentDescription = "Exit to Menu",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LocaleHelper.exitLeaveBtn(isPersian),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 2. Primary Action: Stay / Continue Playing (Teal 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E))
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        showExitConfirmDialog = false
                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.play),
                                        contentDescription = "Stay",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LocaleHelper.exitStayBtn(isPersian),
                                        color = Color.White,
                                        fontSize = 15.sp,
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
                                text = LocaleHelper.settingsTitle(isPersian),
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

                        // Sound & Haptic Toggle Icons (Clean floating row without container box)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Sound Speaker Toggle
                            Image(
                                painter = painterResource(id = R.drawable.sound),
                                contentDescription = "Sound",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable {
                                        val next = !isSoundEnabled
                                        isSoundEnabled = next
                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setSoundEnabled(context, next)
                                        if (next) {
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                        }
                                    },
                                colorFilter = if (!isSoundEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                alpha = if (isSoundEnabled) 1f else 0.4f
                            )

                            Spacer(modifier = Modifier.width(44.dp))

                            // 2. Haptic Vibration Toggle
                            Image(
                                painter = painterResource(id = R.drawable.vibrate),
                                contentDescription = "Vibration",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable {
                                        val next = !isHapticEnabled
                                        isHapticEnabled = next
                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setHapticEnabled(context, next)
                                        if (next) {
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateLight(context)
                                        }
                                    },
                                colorFilter = if (!isHapticEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                alpha = if (isHapticEnabled) 1f else 0.4f
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme Selector Section
                        com.turkce.kelimesolitaire.presentation.ui.components.ThemeSelectorSection(
                            isPersian = isPersian,
                            fontFamily = nunitoFont,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons Column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Open Store Pill (Gold 3D Gradient)
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
                                    .padding(vertical = 14.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.buy),
                                        contentDescription = "Store",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = LocaleHelper.storeTitle(isPersian),
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 2. Side-by-Side: Exit to Menu & Restart Level
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Exit to Main Menu (Red 3D Gradient)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(6.dp, RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))
                                            )
                                        )
                                        .border(1.5.dp, Color(0xFFFDA4AF).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                        .clickable {
                                            showHamburgerMenu = false
                                            onBackToMenu()
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.exit),
                                            contentDescription = "Main Menu",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isPersian) "منوی اصلی" else "Ana Menü",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = nunitoFont
                                        )
                                    }
                                }

                                // Restart Level (Teal 3D Gradient)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
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
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.restart),
                                            contentDescription = "Restart",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isPersian) "شروع مجدد" else "Tekrar",
                                            color = Color.White,
                                            fontSize = 15.sp,
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
                            text = LocaleHelper.formatNumber(item.text, isPersian),
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

        // 6. BOOSTER UNLOCK SPOTLIGHT MODAL
        if (activeTutorial != null && activeTutorial != com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType.LEVEL_1_GUIDE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(500f)
            ) {
                com.turkce.kelimesolitaire.presentation.ui.components.BoosterUnlockDialog(
                    tutorialType = activeTutorial,
                    isPersian = isPersian,
                    fontFamily = nunitoFont,
                    onDismiss = onDismissTutorial
                )
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
