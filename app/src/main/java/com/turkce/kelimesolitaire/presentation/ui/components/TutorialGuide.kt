package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import kotlin.math.roundToInt

// ---------------------------------------------------------------------
// Tutorial logic (stateless: derived from the current board)
// ---------------------------------------------------------------------

enum class TutorialAction { DRAG_TO_SLOT, DRAG_TO_COLUMN, TAP_STOCK }
enum class TutorialMessage {
    DRAG_CATEGORY, DRAG_WORD, TAP_STOCK,
    STACK_ON_SAME, USE_EMPTY_COLUMN, STOCK_TO_COLUMN, CATEGORY_ON_COLUMN
}

data class TutorialHint(
    val action: TutorialAction,
    val cardId: String?,
    val slotId: Int?,
    val message: TutorialMessage,
    val columnIdx: Int? = null
)

/** Returns the single next correct move for the fixed level 1, or null if nothing is left. */
fun computeLevel1Hint(
    slots: List<FoundationSlot>,
    tableau: List<List<SolitaireCard>>,
    waste: List<SolitaireCard>,
    stock: List<SolitaireCard>
): TutorialHint? {
    fun slotFor(card: SolitaireCard) = slots.firstOrNull { it.activeCategory?.id == card.categoryId }

    // 1) A word that can go to an already-active category slot
    val wasteTop = waste.lastOrNull()
    if (wasteTop != null && !wasteTop.isCategory) {
        slotFor(wasteTop)?.let {
            return TutorialHint(TutorialAction.DRAG_TO_SLOT, wasteTop.id, it.id, TutorialMessage.DRAG_WORD)
        }
    }
    for (col in tableau) {
        val c = col.lastOrNull() ?: continue
        if (c.isFaceUp && !c.isCategory) {
            slotFor(c)?.let {
                return TutorialHint(TutorialAction.DRAG_TO_SLOT, c.id, it.id, TutorialMessage.DRAG_WORD)
            }
        }
    }

    // 2) A face-up category card to an empty slot
    val emptySlot = slots.firstOrNull { it.activeCategory == null }
    if (emptySlot != null) {
        for (col in tableau) {
            val c = col.lastOrNull() ?: continue
            if (c.isFaceUp && c.isCategory) {
                return TutorialHint(TutorialAction.DRAG_TO_SLOT, c.id, emptySlot.id, TutorialMessage.DRAG_CATEGORY)
            }
        }
    }

    // 3) Nothing to move: draw from the stock
    if (stock.isNotEmpty() || waste.isNotEmpty()) {
        return TutorialHint(TutorialAction.TAP_STOCK, null, null, TutorialMessage.TAP_STOCK)
    }
    return null
}

/** Next correct move for the fixed level 2. Priority order = teaching order. */
fun computeLevel2Hint(
    slots: List<FoundationSlot>,
    tableau: List<List<SolitaireCard>>,
    waste: List<SolitaireCard>,
    stock: List<SolitaireCard>
): TutorialHint? {
    fun activeSlotFor(card: SolitaireCard) =
        slots.firstOrNull { it.activeCategory?.id == card.categoryId }
    val emptySlot = slots.firstOrNull { it.activeCategory == null }
    val wasteTop = waste.lastOrNull()

    // Start index of the face-up same-category run at the bottom of a column
    fun runStart(col: List<SolitaireCard>): Int {
        var s = col.size - 1
        while (s > 0 && col[s - 1].isFaceUp && col[s - 1].categoryId == col.last().categoryId) s--
        return s
    }
    fun coversFaceDown(col: List<SolitaireCard>): Boolean {
        val s = runStart(col)
        return s > 0 && !col[s - 1].isFaceUp
    }
    fun targetColumnFor(card: SolitaireCard, exceptCol: Int): Int? =
        tableau.indices.firstOrNull { i ->
            val b = tableau[i].lastOrNull()
            i != exceptCol && b != null && b.isFaceUp && !b.isCategory && b.categoryId == card.categoryId
        }

    // 1) Waste word -> active slot
    if (wasteTop != null && !wasteTop.isCategory) {
        activeSlotFor(wasteTop)?.let {
            return TutorialHint(TutorialAction.DRAG_TO_SLOT, wasteTop.id, it.id, TutorialMessage.DRAG_WORD)
        }
    }

    // 2) Column-bottom word (or group) -> active slot
    for (col in tableau) {
        val b = col.lastOrNull() ?: continue
        if (b.isFaceUp && !b.isCategory) {
            activeSlotFor(b)?.let {
                return TutorialHint(TutorialAction.DRAG_TO_SLOT, b.id, it.id, TutorialMessage.DRAG_WORD)
            }
        }
    }

    // 3) Stack onto a same-category card to free the face-down card underneath
    for ((i, col) in tableau.withIndex()) {
        val b = col.lastOrNull() ?: continue
        if (b.isFaceUp && !b.isCategory && coversFaceDown(col)) {
            targetColumnFor(b, i)?.let {
                return TutorialHint(TutorialAction.DRAG_TO_COLUMN, b.id, null, TutorialMessage.STACK_ON_SAME, it)
            }
        }
    }

    // 4) Category card at a column bottom -> empty slot
    if (emptySlot != null) {
        for (col in tableau) {
            val b = col.lastOrNull() ?: continue
            if (b.isFaceUp && b.isCategory) {
                return TutorialHint(TutorialAction.DRAG_TO_SLOT, b.id, emptySlot.id, TutorialMessage.DRAG_CATEGORY)
            }
        }
    }

    // 5) Card covering a face-down card + an empty column exists -> use the empty column
    val emptyCol = tableau.indexOfFirst { it.isEmpty() }
    if (emptyCol != -1) {
        for (col in tableau) {
            val b = col.lastOrNull() ?: continue
            if (b.isFaceUp && !b.isCategory && coversFaceDown(col)) {
                return TutorialHint(TutorialAction.DRAG_TO_COLUMN, b.id, null, TutorialMessage.USE_EMPTY_COLUMN, emptyCol)
            }
        }
    }

    // 6) Waste card (word or category) -> same-category column
    if (wasteTop != null) {
        targetColumnFor(wasteTop, -1)?.let {
            val msg = if (wasteTop.isCategory) TutorialMessage.CATEGORY_ON_COLUMN
            else TutorialMessage.STOCK_TO_COLUMN
            return TutorialHint(TutorialAction.DRAG_TO_COLUMN, wasteTop.id, null, msg, it)
        }
    }

    // 7) Waste category -> empty slot
    if (wasteTop != null && wasteTop.isCategory && emptySlot != null) {
        return TutorialHint(TutorialAction.DRAG_TO_SLOT, wasteTop.id, emptySlot.id, TutorialMessage.DRAG_CATEGORY)
    }

    // 8) Draw from stock
    if (stock.isNotEmpty() || waste.isNotEmpty()) {
        return TutorialHint(TutorialAction.TAP_STOCK, null, null, TutorialMessage.TAP_STOCK)
    }
    return null
}
// ---------------------------------------------------------------------
// Instruction bubble (sits where the old banner was)
// ---------------------------------------------------------------------

@Composable
fun TutorialBubble(
    message: TutorialMessage?,
    isPersian: Boolean,
    fontFamily: FontFamily,
    onSkip: () -> Unit
) {
    val text = when (message) {
        TutorialMessage.DRAG_CATEGORY ->
            if (isPersian) "👑 کارت دسته را بکش و روی جایگاه خالی بالا بگذار"
            else "👑 Kategori kartını yukarıdaki boş yuvaya sürükle"
        TutorialMessage.DRAG_WORD ->
            if (isPersian) "✨ حالا کلمه‌ای از همین دسته را روی آن بگذار"
            else "✨ Şimdi bu kategoriye ait bir kelimeyi üstüne bırak"
        TutorialMessage.TAP_STOCK ->
            if (isPersian) "🃏 حرکتی نداری؟ روی دسته کارت بزن. حرکت‌ها محدودند، با دقت بچین!"
            else "🃏 Hamle yok mu? Desteye dokun. Hamleler sınırlı, dikkatli oyna!"
        TutorialMessage.STACK_ON_SAME ->
            if (isPersian) "🔄 کارت را روی کارت هم‌دسته در ستون دیگر بگذار تا کارت برگشته‌ی زیرش باز شود"
            else "🔄 Kartı başka sütundaki aynı kategorili kartın üstüne koy, altındaki kapalı kart açılsın"
        TutorialMessage.USE_EMPTY_COLUMN ->
            if (isPersian) "📥 ستون خالی داری! کارت را روی آن بگذار تا کارت زیرش باز شود"
            else "📥 Boş sütun var! Kartı oraya koy, altındaki kart açılsın"
        TutorialMessage.STOCK_TO_COLUMN ->
            if (isPersian) "🃏 کارتی که از دسته کشیدی را هم می‌شود روی ستون هم‌دسته‌اش گذاشت"
            else "🃏 Desteden çektiğin kartı da aynı kategorili sütuna koyabilirsin"
        TutorialMessage.CATEGORY_ON_COLUMN ->
            if (isPersian) "👑 کارت دسته را هم می‌شود روی ستون گذاشت، ولی دیگر هیچ کارتی روی آن نمی‌نشیند"
            else "👑 Kategori kartı da sütuna konabilir, ama üstüne artık kart konamaz"
        null ->
            if (isPersian) "👏 آفرین!" else "👏 Aferin!"
    }
    val skipText = if (isPersian) "رد کردن" else "Atla"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .heightIn(min = 64.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A).copy(alpha = 0.95f), Color(0xFF1E293B).copy(alpha = 0.95f))
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF38BDF8), Color(0xFFFFD700))),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC),
                fontFamily = fontFamily,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = skipText,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontFamily = fontFamily,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkip() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------
// Spotlight + animated finger
// ---------------------------------------------------------------------

// Fingertip position inside the hand image (fractions of its size).
// If the finger doesn't land exactly on the card, tune these two numbers.
private const val HAND_TIP_X = 0.35f
private const val HAND_TIP_Y = 0.05f

@Composable
fun TutorialSpotlightOverlay(
    hint: TutorialHint?,
    sourceBounds: Rect?,
    targetBounds: Rect?,
    isDragging: Boolean,
    isPersian: Boolean,
    fontFamily: FontFamily,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val transition = rememberInfiniteTransition(label = "tutorialGuide")
    val raw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0f at 0
                0f at 350
                1f at 1350
                1f at 1800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "handMove"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "handPulse"
    )
    val density = LocalDensity.current

    // Everything is drawn in absolute coordinates, so force LTR (also for Persian)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { origin = it.positionInRoot() }
        ) {
            if (hint != null && sourceBounds != null) {
                val src = sourceBounds.translate(-origin)
                val dst = targetBounds?.translate(-origin)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                ) {
                    val pad = 6.dp.toPx()
                    val corner = CornerRadius(14.dp.toPx())
                    // While dragging we don't dim (the dragged card must stay bright)
                    if (!isDragging) drawRect(Color.Black.copy(alpha = 0.62f))
                    val rects = if (isDragging) listOfNotNull(dst) else listOfNotNull(src, dst)
                    rects.forEach { r ->
                        val tl = Offset(r.left - pad, r.top - pad)
                        val sz = Size(r.width + 2 * pad, r.height + 2 * pad)
                        if (!isDragging) {
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = tl,
                                size = sz,
                                cornerRadius = corner,
                                blendMode = BlendMode.Clear
                            )
                        }
                        drawRoundRect(
                            color = Color(0xFFFFD700),
                            topLeft = tl,
                            size = sz,
                            cornerRadius = corner,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                if (!isDragging) {
                    val handSize = 56.dp
                    val handPx = with(density) { handSize.toPx() }
                    val e = FastOutSlowInEasing.transform(raw)
                    val from = src.center
                    val to = dst?.center ?: from
                    val isDrag = hint.action != TutorialAction.TAP_STOCK
                    val pos = if (isDrag) {
                        Offset(from.x + (to.x - from.x) * e, from.y + (to.y - from.y) * e)
                    } else from
                    val handScale = if (isDrag) 1f else pulse

                    Image(
                        painter = painterResource(id = R.drawable.hand_pointer),
                        contentDescription = null,
                        modifier = Modifier
                            .size(handSize)
                            .absoluteOffset {
                                IntOffset(
                                    (pos.x - HAND_TIP_X * handPx).roundToInt(),
                                    (pos.y - HAND_TIP_Y * handPx).roundToInt()
                                )
                            }
                            .graphicsLayer {
                                scaleX = handScale
                                scaleY = handScale
                            }
                    )
                }
            }
            // Instruction bubble: drawn above the dim layer, in the empty area below the columns
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 190.dp)   // پایین‌تر از ستون‌ها؛ در صورت نیاز تنظیم کن
                    .padding(horizontal = 8.dp)
            ) {
                TutorialBubble(
                    message = hint?.message,
                    isPersian = isPersian,
                    fontFamily = fontFamily,
                    onSkip = onSkip
                )
            }
        }
    }
}