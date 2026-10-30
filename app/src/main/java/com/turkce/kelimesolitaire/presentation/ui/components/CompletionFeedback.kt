package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val SparkleGold = Color(0xFFFFD54A)
private val SparkleLight = Color(0xFFFFF3B0)

/** Golden star-shaped sparkles flying outward from the center of its box. */
@Composable
fun SparkleBurst(
    trigger: Int,                 // change this value to fire a new burst
    count: Int = 6,
    maxDistanceDp: Float = 46f,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(1f) }
    val angles = remember(trigger) {
        val rng = Random(trigger * 31 + count)
        val base = rng.nextFloat() * 2f * PI.toFloat()
        List(count) { i ->
            base + i * (2f * PI.toFloat() / count) + rng.nextFloat() * 0.5f
        }
    }
    val sizes = remember(trigger) {
        val rng = Random(trigger * 17 + count)
        List(count) { 0.7f + rng.nextFloat() * 0.8f }
    }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(650, easing = LinearOutSlowInEasing))
        }
    }

    if (progress.value >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxDist = maxDistanceDp.dp.toPx()
        val alpha = (1f - p).coerceIn(0f, 1f)
        val scale = if (p < 0.2f) p / 0.2f else 1f - (p - 0.2f) * 0.5f

        for (i in 0 until count) {
            val dist = maxDist * p
            val pos = Offset(
                center.x + cos(angles[i]) * dist,
                center.y + sin(angles[i]) * dist
            )
            val r = 6.dp.toPx() * sizes[i] * scale
            val star = Path().apply {
                for (k in 0 until 8) {
                    val rad = if (k % 2 == 0) r else r * 0.38f
                    val a = (k * 45f - 90f) * PI.toFloat() / 180f
                    val x = pos.x + cos(a) * rad
                    val y = pos.y + sin(a) * rad
                    if (k == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(star, SparkleGold.copy(alpha = alpha))
            drawCircle(SparkleLight.copy(alpha = alpha * 0.9f), r * 0.28f, pos)
        }
    }
}
private val StickerEmojis = listOf("👍", "👌", "🤌", "👏", "🫶", "🔥", "⭐", "🎉", "💪", "✨")
private val StickerTextsFa = listOf("آفرین!", "عالی!", "فوق‌العاده!", "دمت گرم!", "حرف نداره!")
private val StickerTextsTr = listOf("Aferin!", "Harika!", "Süper!", "Bravo!", "Mükemmel!")
private val ComboEmojis = listOf("🔥", "🎉", "🤩", "💥")
private val ComboTextsFa = listOf("کمبو!", "یه‌جا!", "ضربه‌ی طلایی!")
private val ComboTextsTr = listOf("Kombo!", "Tek Hamle!", "Muhteşem!")

/**
 * Sticker that pops in, floats up and fades. Shows EITHER an emoji OR a short text (random).
 * Call with a new `trigger` for each completion.
 */
@Composable
fun CompletionSticker(
    trigger: Int,
    isPersian: Boolean,
    isCombo: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (trigger <= 0) return

    // Pick once per trigger: emoji (50%) or text (50%)
    val pick = remember(trigger) {
        val rng = Random(trigger * 13 + 5)
        val useEmoji = rng.nextBoolean()
        if (useEmoji) {
            val list = if (isCombo) ComboEmojis else StickerEmojis
            Pair(true, list[rng.nextInt(list.size)])
        } else {
            val list = when {
                isCombo && isPersian -> ComboTextsFa
                isCombo -> ComboTextsTr
                isPersian -> StickerTextsFa
                else -> StickerTextsTr
            }
            Pair(false, list[rng.nextInt(list.size)])
        }
    }

    val scale = remember(trigger) { Animatable(0.2f) }
    val alpha = remember(trigger) { Animatable(0f) }
    val lift = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        launch { alpha.animateTo(1f, tween(120)) }
        launch {
            scale.animateTo(
                if (isCombo) 1.25f else 1.1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        kotlinx.coroutines.delay(800)
        launch { lift.animateTo(-40f, tween(450, easing = FastOutSlowInEasing)) }
        alpha.animateTo(0f, tween(450))
    }

    Box(
        modifier = modifier
            .offset(y = lift.value.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            },
        contentAlignment = Alignment.Center
    ) {
        if (pick.first) {
            Text(text = pick.second, fontSize = if (isCombo) 40.sp else 34.sp)
        } else {
            OutlinedText(
                text = pick.second,
                textColor = Color.White,
                outlineColor = Color(0xFF78350F),
                outlineWidth = 8f,
                fontSize = if (isCombo) 20.sp else 16.sp,
                fontWeight = FontWeight.Black,
                shadowOffsetY = 2f
            )
        }
    }
}