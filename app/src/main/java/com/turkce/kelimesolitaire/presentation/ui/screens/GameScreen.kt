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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
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
import com.turkce.kelimesolitaire.presentation.ui.components.candyGloss
import com.turkce.kelimesolitaire.presentation.ui.components.specularGlint
import com.turkce.kelimesolitaire.presentation.ui.theme.GameTableBg
import com.turkce.kelimesolitaire.presentation.ui.theme.CardBackBg
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleBg
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.StockRecycleText
import com.turkce.kelimesolitaire.presentation.ui.theme.LevelTitleText
import com.turkce.kelimesolitaire.presentation.ui.theme.LevelTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCardBg
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCardBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesTitleText
import com.turkce.kelimesolitaire.presentation.ui.theme.MovesCountText
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupHeaderBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonShadow
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayDepth
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnTop
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnMid
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameBase
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.turkce.kelimesolitaire.presentation.ui.components.ThemeIntroSheet
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.viewmodel.TutorialType
import com.turkce.kelimesolitaire.presentation.ui.components.TutorialAction
import com.turkce.kelimesolitaire.presentation.ui.components.TutorialSpotlightOverlay
import com.turkce.kelimesolitaire.presentation.ui.components.computeLevel1Hint
import com.turkce.kelimesolitaire.presentation.ui.components.SquareMenuButton
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameGradient
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.components.CoinCounter
import com.turkce.kelimesolitaire.presentation.ui.components.TopBarIconButton
import com.turkce.kelimesolitaire.presentation.ui.components.computeLevel2Hint
import com.turkce.kelimesolitaire.presentation.ui.components.PopupFrame
import com.turkce.kelimesolitaire.presentation.ui.components.CandyButton
import com.turkce.kelimesolitaire.presentation.ui.components.MenuWideButton
import com.turkce.kelimesolitaire.presentation.ui.components.MenuIcon
import com.turkce.kelimesolitaire.presentation.ui.components.IconLabelRow
import com.turkce.kelimesolitaire.presentation.ui.components.PopupButtonLabel
import com.turkce.kelimesolitaire.presentation.ui.components.MenuRedBrush
import com.turkce.kelimesolitaire.presentation.ui.components.MenuRedOutline
import com.turkce.kelimesolitaire.presentation.ui.components.SettingsDialog
import com.turkce.kelimesolitaire.presentation.ui.components.ThemeDialog
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.TvIcon


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
    modifier: Modifier = Modifier,
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
    onDismissTutorial: () -> Unit = {},
    onDismissCategoryCelebration: () -> Unit = {},
    shouldAnimateDeal: Boolean = true,
    onWatchAdForMoves: () -> Unit = {},
    onClaimThemeReward: (Int) -> Unit = {}
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




    // List of floating coins text (+2) to animate
    var showHamburgerMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isSoundEnabled by rememberSaveable {
        mutableStateOf(
            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isSoundEnabled(
                context
            )
        )
    }
    var isHapticEnabled by rememberSaveable {
        mutableStateOf(
            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isHapticEnabled(
                context
            )
        )
    }

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
    val isWasteDragging =
        wastePile.lastOrNull()?.let { top -> draggedCards.any { it.id == top.id } } ?: false

    // Card Dealing Animation state at level start / restart (bypassed if entering in-progress game)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var isDealingFinished by remember(
        levelData.levelNumber,
        shouldAnimateDeal
    ) { mutableStateOf(!shouldAnimateDeal) }
    val dealAnimProgress = remember(
        levelData.levelNumber,
        shouldAnimateDeal
    ) { Animatable(if (shouldAnimateDeal) 0f else 1f) }

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
                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(
                    context
                )
            }
        }
    }

    val cardBounds = remember { mutableStateMapOf<String, Rect>() }
    val isTutorialMode = activeTutorial == TutorialType.LEVEL_1_GUIDE ||
            activeTutorial == TutorialType.LEVEL_2_GUIDE
    val tutorialHint =
        if (isTutorialMode && isDealingFinished && !isLevelWon && !showOutofMovesDialog) {
            if (activeTutorial == TutorialType.LEVEL_1_GUIDE)
                computeLevel1Hint(foundationSlots, tableauPiles, wastePile, stockPile)
            else
                computeLevel2Hint(foundationSlots, tableauPiles, wastePile, stockPile)
        } else null
    val wrongMoveText = if (isPersian) "فقط کارت نشان‌داده‌شده را همان‌جایی که نشان داده شده بگذار 👆"
    else "Sadece gösterilen kartı gösterilen yere koy 👆"

    val guardedCardDropped: (List<SolitaireCard>, FoundationSlot) -> Boolean = { cards, slot ->
        if (isTutorialMode && tutorialHint?.action == TutorialAction.DRAG_TO_COLUMN) {
            onShowMessage(wrongMoveText, MessageType.INFO)
            false
        } else onCardDropped(cards, slot)
    }

    val guardedCardStacked: (List<SolitaireCard>, Int) -> Boolean = { cards, col ->
        if (isTutorialMode && tutorialHint != null && tutorialHint.columnIdx != col) {
            onShowMessage(wrongMoveText, MessageType.INFO)
            false
        } else onCardStacked(cards, col)
    }

    // Theme intro: shown once, after the deal animation, from level 6 onward
    var showThemeIntro by remember { mutableStateOf(false) }
    LaunchedEffect(isDealingFinished, levelData.levelNumber) {
        if (isDealingFinished &&
            activeTutorial == null &&
            !showOutofMovesDialog &&
            !isLevelWon &&
            GameSettingsManager.shouldShowThemeIntro(context, levelData.levelNumber)
        ) {
            delay(600L)
            showThemeIntro = true
            GameSettingsManager.markThemeIntroShown(context)
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Coins Status Box (With border, no background)
                    CoinCounter(
                        coins,
                        isPersian,
                        onClick = { onOpenStore() }
                    )
                    // Center: Level Title
                    OutlinedText(
                        text = LocaleHelper.levelTitle(levelData.levelNumber, isPersian),
                        textColor = LevelTitleText,
                        outlineColor = LevelTitleOutline,
                        outlineWidth = 10f,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Right: Settings Icon - 3D Button like Sound & Vibration
                    TopBarIconButton(
                        iconRes = R.drawable.setting,
                        contentDescription = "Settings",
                        onClick = {
                            showHamburgerMenu = true
                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(
                                context
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // LEVEL 1 INTERACTIVE TUTORIAL BANNER


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
                val remainingInDeck = stockPile.size +
                        wastePile.count { it.categoryId != "joker_wildcard" }
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

                            val isWasteHinted = hintedCardId == topWaste.id
                            WordCard(
                                card = topWaste,
                                isSelected = selectedCardId == topWaste.id || isDragged,
                                isShaking = shakingCardId == topWaste.id,
                                isShattering = shatteringJokerId == topWaste.id,
                                isHinted = isWasteHinted,
                                isDragged = isDragged,
                                dragOffsetProvider = { dragOffset },
                                isInteractionEnabled = !isLevelWon && isDealingFinished && (!isTutorialMode || tutorialHint?.cardId == topWaste.id) && movesRemaining > 0 && !showOutofMovesDialog && !isAnimatingReturn && (draggedCards.isEmpty() || isDragged) && !showThemeIntro,
                                onTap = {},
                                onDragStart = {
                                    draggedCards = listOf(topWaste)
                                    dragOffset = Offset.Zero
                                },
                                onDrag = { dragAmount ->
                                    dragOffset = Offset(
                                        dragOffset.x + dragAmount.x,
                                        dragOffset.y + dragAmount.y
                                    )
                                },
                                onDragEnd = { dropCenter ->
                                    val finalGroup = draggedCards
                                    if (finalGroup.isNotEmpty()) {
                                        val testPoints = if (finalGroup.size > 1) {
                                            listOf(
                                                dropCenter,
                                                Offset(
                                                    dropCenter.x,
                                                    dropCenter.y - ((finalGroup.size - 1) * 35f)
                                                )
                                            )
                                        } else {
                                            listOf(dropCenter)
                                        }
                                        val matchedSlot = findBestFoundationSlot(
                                            foundationSlots,
                                            dropZoneBounds,
                                            testPoints
                                        )
                                        if (matchedSlot != null) {
                                            val success = guardedCardDropped(finalGroup, matchedSlot)
                                            if (success) {
                                                draggedCards = emptyList()
                                                if (isHapticEnabled) {
                                                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(
                                                        context
                                                    )
                                                }
                                                if (isSoundEnabled) {
                                                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(
                                                        context
                                                    )
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    isAnimatingReturn = true
                                                    val anim = Animatable(
                                                        dragOffset,
                                                        Offset.VectorConverter
                                                    )
                                                    anim.animateTo(
                                                        Offset.Zero,
                                                        spring(stiffness = Spring.StiffnessMedium)
                                                    ) {
                                                        dragOffset = this.value
                                                    }
                                                    draggedCards = emptyList()
                                                    onCardSelected(null)
                                                    isAnimatingReturn = false
                                                }
                                            }
                                        } else {
                                            val matchedColIdx =
                                                findBestTableauColumn(tableauBounds, testPoints)
                                            if (matchedColIdx != -1) {
                                                val success =
                                                    guardedCardStacked(finalGroup, matchedColIdx)
                                                if (success) {
                                                    draggedCards = emptyList()
                                                    if (isHapticEnabled) {
                                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(
                                                            context
                                                        )
                                                    }
                                                    if (isSoundEnabled) {
                                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(
                                                            context
                                                        )
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        isAnimatingReturn = true
                                                        val anim = Animatable(
                                                            dragOffset,
                                                            Offset.VectorConverter
                                                        )
                                                        anim.animateTo(
                                                            Offset.Zero,
                                                            spring(stiffness = Spring.StiffnessMedium)
                                                        ) {
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
                                                    val anim = Animatable(
                                                        dragOffset,
                                                        Offset.VectorConverter
                                                    )
                                                    anim.animateTo(
                                                        Offset.Zero,
                                                        spring(stiffness = Spring.StiffnessMedium)
                                                    ) {
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
                                        anim.animateTo(
                                            Offset.Zero,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        ) {
                                            dragOffset = this.value
                                        }
                                        draggedCards = emptyList()
                                        isAnimatingReturn = false
                                    }
                                },
                                modifier = Modifier
                                    .zIndex(if (isDragged) 100f else 0f)
                                    .onGloballyPositioned { c ->
                                        val b = c.boundsInRoot()
                                        if (cardBounds[topWaste.id] != b) cardBounds[topWaste.id] =
                                            b
                                    }
                                    .then(
                                        if (isWasteHinted) Modifier.border(
                                            2.5.dp,
                                            Color(0xFFF1C40F),
                                            RoundedCornerShape(12.dp)
                                        )
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
                            Text(
                                text = LocaleHelper.emptySlot(isPersian),
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Stock card slot
                    val isStockHinted = hintedCardId == "stock_pile"
                    if (stockPile.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 110.dp)
                                .clickable(enabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog && !showThemeIntro && ((!isTutorialMode || tutorialHint == null || tutorialHint.action == TutorialAction.TAP_STOCK))) { onDrawFromStock() }
                                .onGloballyPositioned { c ->
                                    val b = c.boundsInRoot()
                                    if (cardBounds["stock_pile"] != b) cardBounds["stock_pile"] = b
                                },
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
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
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
                                        .border(
                                            1.2.dp,
                                            Color.White.copy(alpha = 0.7f),
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                            // Top Layer (WordCard)
                            Box(
                                modifier = Modifier
                                    .size(width = 85.dp, height = 110.dp)
                                    .then(
                                        if (isStockHinted) Modifier.border(
                                            2.5.dp,
                                            Color(0xFFF1C40F),
                                            RoundedCornerShape(8.dp)
                                        )
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
                            // Remaining count (plain number, no circle)

                            OutlinedText(
                                text = LocaleHelper.formatNumber(remainingInDeck, isPersian),
                                textColor = Color.White,
                                outlineColor = MenuBtnOutline,
                                outlineWidth = 8f,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 6.dp, bottom = 4.dp)
                            )
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
                                .clickable(enabled = !isLevelWon && isDealingFinished && movesRemaining > 0 && !showOutofMovesDialog && !showThemeIntro && (!isTutorialMode || tutorialHint?.action == TutorialAction.TAP_STOCK)) { onDrawFromStock() }
                                .onGloballyPositioned { c ->
                                    val b = c.boundsInRoot()
                                    if (cardBounds["stock_pile"] != b) cardBounds["stock_pile"] = b
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isPersian) "♻️\nبر زدن" else "♻️\nYenile",
                                    color = StockRecycleText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = LocaleHelper.formatNumber(remainingInDeck, isPersian),
                                    color = StockRecycleText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            }
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
                    val isColDragging =
                        draggedCards.isNotEmpty() && colList.any { c -> draggedCards.any { it.id == c.id } }
                    val isColHinted = hintedTargetId == "col_$colIdx"

                    Column(
                        modifier = Modifier
                            .width(85.dp)
                            .fillMaxHeight()
                            .zIndex(if (isColDragging) 10f else 1f)
                            .then(
                                if (isColHinted) Modifier.border(
                                    2.dp,
                                    Color(0xFFF1C40F).copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
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
                                if (!isDealingFinished) return@CategoryDropZone
                                selectedCardId?.let { cardId ->
                                    val cardFromWaste =
                                        wastePile.lastOrNull()?.takeIf { it.id == cardId }

                                    var cardFromTableau: SolitaireCard? = null
                                    var sourceColIdx = -1
                                    var sourceRowIdx = -1

                                    for (cI in 0..3) {
                                        val cList = tableauPiles[cI]
                                        val idx =
                                            cList.indexOfFirst { it.id == cardId && it.isFaceUp }
                                        if (idx != -1) {
                                            cardFromTableau = cList[idx]
                                            sourceColIdx = cI
                                            sourceRowIdx = idx
                                            break
                                        }
                                    }

                                    val targetCard = cardFromTableau
                                    if (cardFromWaste != null) {
                                        guardedCardDropped(listOf(cardFromWaste), slot)
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
                                        guardedCardDropped(group, slot)
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
                                                val cardFromWaste = wastePile.lastOrNull()
                                                    ?.takeIf { it.id == cardId }
                                                var cardFromTableau: SolitaireCard? = null
                                                var sourceColIdx = -1
                                                var sourceRowIdx = -1

                                                for (cI in 0..3) {
                                                    val cList = tableauPiles[cI]
                                                    val idx =
                                                        cList.indexOfFirst { it.id == cardId && it.isFaceUp }
                                                    if (idx != -1) {
                                                        cardFromTableau = cList[idx]
                                                        sourceColIdx = cI
                                                        sourceRowIdx = idx
                                                        break
                                                    }
                                                }

                                                val targetCard = cardFromTableau
                                                if (cardFromWaste != null) {
                                                    guardedCardStacked(listOf(cardFromWaste), colIdx)
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
                                                    val group = sourceList.subList(
                                                        startIdx,
                                                        sourceList.size
                                                    )
                                                    guardedCardStacked(group, colIdx)
                                                }
                                            }
                                        }
                                )
                            }

                            colList.forEachIndexed { rowIdx, card ->
                                key(card.id) {
                                    val dealIndex = rowIdx * 4 + colIdx
                                    val totalTableauCards = remember(tableauPiles) {
                                        tableauPiles.mapIndexed { cIdx, list -> list.indices.map { rIdx -> rIdx * 4 + cIdx } }
                                            .flatten().maxOrNull() ?: 1
                                    }
                                    val cardStart =
                                        if (totalTableauCards > 0) (dealIndex.toFloat() / (totalTableauCards + 2.5f)) * 0.56f else 0f
                                    val cardDuration = 0.44f
                                    val cardProgress =
                                        if (isDealingFinished) 1f else ((dealAnimProgress.value - cardStart) / cardDuration).coerceIn(
                                            0f,
                                            1f
                                        )
                                    val eased = FastOutSlowInEasing.transform(cardProgress)

                                    val currentAlpha =
                                        if (isDealingFinished) 1f else if (cardProgress <= 0f) 0f else minOf(
                                            1f,
                                            cardProgress * 3.5f
                                        )
                                    val dirMultiplier = if (isRtl) -1f else 1f
                                    val startOffsetX = dirMultiplier * ((3 - colIdx) * 88f + 16f)
                                    val startOffsetY = -(210f + rowIdx * 25f)
                                    val arcLiftY =
                                        if (isDealingFinished) 0.dp else (kotlin.math.sin(
                                            cardProgress * Math.PI
                                        ).toFloat() * -24f).dp
                                    val currentDealX =
                                        if (isDealingFinished) 0.dp else (startOffsetX * (1f - eased)).dp
                                    val currentDealY =
                                        if (isDealingFinished) (rowIdx * 25).dp else (((rowIdx * 25f) + startOffsetY * (1f - eased)).dp + arcLiftY)
                                    val startRotation =
                                        dirMultiplier * (if (colIdx < 2) (-14f + colIdx * 4f) else (6f + (colIdx - 2) * 5f))
                                    val currentRotation =
                                        if (isDealingFinished) 0f else (startRotation * (1f - eased))
                                    val currentScale =
                                        if (isDealingFinished) 1f else (0.80f + (0.20f * eased))

                                    val isDragged = draggedCards.any { it.id == card.id }
                                    val dragGroupIdx =
                                        if (isDragged) draggedCards.indexOfFirst { it.id == card.id } else -1
                                    val compressionYOffset =
                                        if (dragGroupIdx > 0) (-(dragGroupIdx * 18f * compressionFraction)).dp else 0.dp
                                    val isCardHinted = hintedCardId == card.id

                                    WordCard(
                                        card = card,
                                        isSelected = selectedCardId == card.id || isDragged,
                                        isShaking = shakingCardId == card.id,
                                        isShattering = shatteringJokerId == card.id,
                                        isHinted = isCardHinted,
                                        isDragged = isDragged,
                                        dragZIndex = if (dragGroupIdx >= 0) dragGroupIdx.toFloat() else 0f,
                                        dragOffsetProvider = { dragOffset },
                                        isInteractionEnabled = !isLevelWon && isDealingFinished && ((!isTutorialMode || tutorialHint == null || tutorialHint.cardId == card.id)) && movesRemaining > 0 && !showOutofMovesDialog && !isAnimatingReturn && (draggedCards.isEmpty() || isDragged) && !showThemeIntro,
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
                                            dragOffset = Offset(
                                                dragOffset.x + dragAmount.x,
                                                dragOffset.y + dragAmount.y
                                            )
                                        },
                                        onDragEnd = { dropCenter ->
                                            val finalGroup = draggedCards
                                            if (finalGroup.isNotEmpty()) {
                                                val touchIndexInGroup =
                                                    finalGroup.indexOfFirst { it.id == card.id }
                                                        .coerceAtLeast(0)
                                                val normalStepPx = with(density) { 25.dp.toPx() }
                                                val compressedStepPx =
                                                    with(density) { (25.dp - (18.dp * compressionFraction)).toPx() }
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
                                                    listOf(
                                                        dropCenter,
                                                        topCardCenter,
                                                        bottomCardCenter,
                                                        midCardCenter
                                                    )
                                                } else {
                                                    listOf(dropCenter)
                                                }
                                                val matchedSlot = findBestFoundationSlot(
                                                    foundationSlots,
                                                    dropZoneBounds,
                                                    testPoints
                                                )
                                                if (matchedSlot != null) {
                                                    val success =
                                                        guardedCardDropped(finalGroup, matchedSlot)
                                                    if (success) {
                                                        draggedCards = emptyList()
                                                        if (isHapticEnabled) {
                                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(
                                                                context
                                                            )
                                                        }
                                                        if (isSoundEnabled) {
                                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(
                                                                context
                                                            )
                                                        }
                                                    } else {
                                                        coroutineScope.launch {
                                                            isAnimatingReturn = true
                                                            val anim = Animatable(
                                                                dragOffset,
                                                                Offset.VectorConverter
                                                            )
                                                            anim.animateTo(
                                                                Offset.Zero,
                                                                spring(stiffness = Spring.StiffnessMedium)
                                                            ) {
                                                                dragOffset = this.value
                                                            }
                                                            draggedCards = emptyList()
                                                            onCardSelected(null)
                                                            isAnimatingReturn = false
                                                        }
                                                    }
                                                } else {
                                                    val matchedColIdx = findBestTableauColumn(
                                                        tableauBounds,
                                                        testPoints
                                                    )
                                                    if (matchedColIdx != -1) {
                                                        val success =
                                                            guardedCardStacked(finalGroup, matchedColIdx)
                                                        if (success) {
                                                            draggedCards = emptyList()
                                                            if (isHapticEnabled) {
                                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateCardSnap(
                                                                    context
                                                                )
                                                            }
                                                            if (isSoundEnabled) {
                                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCardSnapSound(
                                                                    context
                                                                )
                                                            }
                                                        } else {
                                                            coroutineScope.launch {
                                                                isAnimatingReturn = true
                                                                val anim = Animatable(
                                                                    dragOffset,
                                                                    Offset.VectorConverter
                                                                )
                                                                anim.animateTo(
                                                                    Offset.Zero,
                                                                    spring(stiffness = Spring.StiffnessMedium)
                                                                ) {
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
                                                            val anim = Animatable(
                                                                dragOffset,
                                                                Offset.VectorConverter
                                                            )
                                                            anim.animateTo(
                                                                Offset.Zero,
                                                                spring(stiffness = Spring.StiffnessMedium)
                                                            ) {
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
                                                val anim =
                                                    Animatable(dragOffset, Offset.VectorConverter)
                                                anim.animateTo(
                                                    Offset.Zero,
                                                    spring(stiffness = Spring.StiffnessMedium)
                                                ) {
                                                    dragOffset = this.value
                                                }
                                                draggedCards = emptyList()
                                                isAnimatingReturn = false
                                            }
                                        },
                                        modifier = Modifier
                                            .offset(
                                                x = currentDealX,
                                                y = currentDealY + compressionYOffset
                                            )
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
                                                            bottom = maxOf(
                                                                boxBounds.bottom,
                                                                cardBounds.bottom + 50f
                                                            )
                                                        )
                                                    } else {
                                                        cardBounds
                                                    }
                                                }
                                            }
                                            .onGloballyPositioned { c ->
                                                val b = c.boundsInRoot()
                                                if (cardBounds[card.id] != b) cardBounds[card.id] =
                                                    b
                                            }
                                            .then(
                                                if (isCardHinted) Modifier.border(
                                                    2.5.dp,
                                                    Color(0xFFF1C40F),
                                                    RoundedCornerShape(12.dp)
                                                )
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
                    horizontalArrangement = Arrangement.spacedBy(
                        28.dp,
                        Alignment.CenterHorizontally
                    ),
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
                        lockText = if (isPersian) "🔒 مرحله ۴" else "🔒 4. Lvl",
                        hasFree = hasFreeUndo,
                        freeCount = freeUndoCount,
                        coinCost = 50,
                        isPersian = isPersian,
                        enabled = !isLevelWon && isDealingFinished,
                        fontFamily = nunitoFont,
                        onClick = {
                            if (!isUndoUnlocked) {
                                onShowMessage(
                                    if (isPersian) "قابلیت بازگشت در مرحله ۴ باز می‌شود! 🔒" else "Geri Al özelliği 4. seviyede açılır! 🔒",
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
// 4. OUT OF MOVES
        if (showOutofMovesDialog) {
            val extraMoves = GameViewModel.EXTRA_MOVES_FOR_COINS
            val cost = GameViewModel.EXTRA_MOVES_COIN_COST
            val goldOutline = Color(0xFF78350F)
            val blueOutline = Color(0xFF0C4A6E)

            PopupFrame(
                title = LocaleHelper.outOfMovesTitle(isPersian),
                titleSize = 25.sp
            ) {
                Text(
                    text = LocaleHelper.outOfMovesPrompt(isPersian),
                    color = PopupTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                CandyButton(
                    onClick = onBuyExtraMoves,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    gradient = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706)),
                    outlineColor = goldOutline,
                    corner = 16.dp
                ) {
                    IconLabelRow(
                        isPersian = isPersian,
                        icon = { CoinIcon(size = 30.dp) },
                        label = {
                            PopupButtonLabel(
                                text = if (isPersian)
                                    "${LocaleHelper.formatNumber(cost, true)} سکه (+${LocaleHelper.formatNumber(extraMoves, true)} حرکت)"
                                else "$cost Altın (+$extraMoves Hamle)",
                                outline = goldOutline,
                                fontSize = 18.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CandyButton(
                    onClick = onWatchAdForMoves,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
                    outlineColor = blueOutline,
                    corner = 16.dp
                ) {
                    IconLabelRow(
                        isPersian = isPersian,
                        icon = { TvIcon(size = 26.dp) },
                        label = {
                            PopupButtonLabel(
                                text = if (isPersian)
                                    "ویدیو (+${LocaleHelper.formatNumber(GameViewModel.EXTRA_MOVES_FOR_AD, true)} حرکت)"
                                else "Video İzle (+${GameViewModel.EXTRA_MOVES_FOR_AD} Hamle)",
                                outline = blueOutline,
                                fontSize = 17.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CandyButton(
                    onClick = onAcceptDefeat,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    gradient = listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFFB91C1C)),
                    outlineColor = MenuRedOutline,
                    corner = 16.dp
                ) {
                    PopupButtonLabel(LocaleHelper.giveUp(isPersian), MenuRedOutline, 18.sp)
                }
            }
        }

// 5. EXIT CONFIRMATION
        if (showExitConfirmDialog) {
            PopupFrame(
                title = LocaleHelper.exitDialogTitle(isPersian),
                onDismiss = { showExitConfirmDialog = false },
                titleSize = 30.sp
            ) {
                Text(
                    text = LocaleHelper.exitDialogPrompt(isPersian),
                    color = PopupTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LocaleHelper.exitDialogDesc(isPersian),
                    color = PopupTextPrimary.copy(alpha = 0.75f),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                MenuWideButton(
                    text = LocaleHelper.exitStayBtn(isPersian),
                    isPersian = isPersian,
                    onClick = {
                        showExitConfirmDialog = false
                        GameSettingsManager.playButtonClickSound(context)
                    },
                    icon = { MenuIcon(R.drawable.play, "Stay") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                MenuWideButton(
                    text = LocaleHelper.exitLeaveBtn(isPersian),
                    isPersian = isPersian,
                    onClick = {
                        showExitConfirmDialog = false
                        onBackToMenu()
                    },
                    face = MenuRedBrush,
                    outline = MenuRedOutline,
                    icon = { MenuIcon(R.drawable.exit, "Exit to Menu") }
                )
            }
        }

// 6. SETTINGS + THEME
        if (showHamburgerMenu) {
            SettingsDialog(
                isPersian = isPersian,
                soundEnabled = isSoundEnabled,
                hapticEnabled = isHapticEnabled,
                onSoundChange = {
                    isSoundEnabled = it
                    GameSettingsManager.setSoundEnabled(context, it)
                },
                onHapticChange = {
                    isHapticEnabled = it
                    GameSettingsManager.setHapticEnabled(context, it)
                },
                onDismiss = { showHamburgerMenu = false },
                onOpenTheme = { showThemeDialog = true },
                onOpenStore = {
                    showHamburgerMenu = false
                    onOpenStore()
                },
                onRestart = {
                    showHamburgerMenu = false
                    onRestartLevel()
                },
                onMainMenu = {
                    showHamburgerMenu = false
                    onBackToMenu()
                }
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                isPersian = isPersian,
                onDismiss = { showThemeDialog = false },
                onThemeSelected = {
                    showThemeDialog = false
                    showHamburgerMenu = false
                }
            )
        }
        // THEME INTRO BOTTOM SHEET (no dark scrim, board stays visible)
        AnimatedVisibility(
            visible = showThemeIntro,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(400f),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ThemeIntroSheet(
                rewardCoins = GameSettingsManager.THEME_REWARD_COINS,
                onDismiss = { showThemeIntro = false },
                onConfirm = {
                    showThemeIntro = false
                    onClaimThemeReward(GameSettingsManager.THEME_REWARD_COINS)
                }
            )
        }

        // TUTORIAL SPOTLIGHT + FINGER
        if (isTutorialMode && !showHamburgerMenu && !showExitConfirmDialog) {
            val srcBounds = tutorialHint?.let {
                if (it.action == TutorialAction.TAP_STOCK) cardBounds["stock_pile"]
                else it.cardId?.let { id -> cardBounds[id] }
            }
            val dstBounds = tutorialHint?.let { h ->
                when {
                    h.slotId != null -> dropZoneBounds[h.slotId.toString()]
                    h.columnIdx != null -> {
                        val last = tableauPiles[h.columnIdx].lastOrNull()
                        if (last != null) cardBounds[last.id]
                        else tableauBounds[h.columnIdx]?.let { b ->
                            val w = with(density) { 85.dp.toPx() }
                            val hgt = with(density) { 110.dp.toPx() }
                            Rect(b.center.x - w / 2, b.top, b.center.x + w / 2, b.top + hgt)
                        }
                    }
                    else -> null
                }
            }
            TutorialSpotlightOverlay(
                hint = tutorialHint,
                sourceBounds = srcBounds,
                targetBounds = dstBounds,
                isDragging = draggedCards.isNotEmpty(),
                isPersian = isPersian,
                fontFamily = nunitoFont,
                onSkip = onDismissTutorial,
                modifier = Modifier.zIndex(300f)
            )
        }


        // 6. BOOSTER UNLOCK SPOTLIGHT MODAL
        if (activeTutorial != null && !isTutorialMode && isDealingFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(500f)
            ) {
                com.turkce.kelimesolitaire.presentation.ui.components.BoosterUnlockDialog(
                    tutorialType = activeTutorial,
                    isPersian = isPersian,
                    fontFamily = nunitoFont,
                    giftCount = when (activeTutorial) {
                        TutorialType.UNDO_UNLOCK -> freeUndoCount
                        TutorialType.HINT_UNLOCK -> freeHintCount
                        else -> freeJokerCount
                    }.coerceAtLeast(1),
                    onDismiss = onDismissTutorial
                )
            }
        }
    }
}

