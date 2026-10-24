package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Color.darker(f: Float = 0.55f): Color = lerp(this, Color.Black, f)

fun Modifier.candyGloss(
    corner: Dp,
    inset: Dp = 2.dp,
    topAlpha: Float = 0.25f,
    bottomAlpha: Float = 0.10f,
    heightFraction: Float = 0.85f
): Modifier = this.drawBehind {
    val h = size.height * heightFraction
    val i = (size.height - h) * 0.50f
    val r = (corner.toPx() - i).coerceAtLeast(0f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = topAlpha),
                Color.White.copy(alpha = bottomAlpha)
            ),
            startY = i,
            endY = i + h
        ),
        topLeft = Offset(i, i),
        size = Size(size.width - 2 * i, h),
        cornerRadius = CornerRadius(r, r)
    )
}

fun Modifier.glassReflection(
    streakAlpha: Float = 0.29f,
    glowAlpha: Float = 0.75f
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    val skew = h * 0.55f

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = glowAlpha), Color.Transparent),
            center = Offset(w * 0.82f, h * 1.02f),
            radius = w * 0.42f
        )
    )

    fun streak(x0: Float, bandW: Float, alpha: Float) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(x0, 0f)
            lineTo(x0 + bandW, 0f)
            lineTo(x0 + bandW - skew, h)
            lineTo(x0 - skew, h)
            close()
        }
        drawPath(path, Color.White.copy(alpha = alpha))
    }
    streak(w * 0.52f, w * 0.105f, streakAlpha)
    streak(w * 0.52f + w * 0.13f, w * 0.03f, streakAlpha * 0.85f)
}

fun Modifier.specularGlint(
    offsetX: Dp = 10.dp,
    offsetY: Dp = 5.dp,
    scale: Float = 1f
): Modifier = this.drawBehind {
    drawOval(
        color = Color.White.copy(alpha = 0.76f),
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = Size(16.dp.toPx() * scale, 6.dp.toPx() * scale)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset((offsetX + 20.dp * scale).toPx(), (offsetY + 1.5.dp * scale).toPx()),
        size = Size(3.dp.toPx() * scale, 3.dp.toPx() * scale)
    )
}

/**
 * Standard Candy Gloss 3D Action Button matching the store pack buttons.
 */
@Composable
fun CandyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> = listOf(Color(0xFFA3E635), Color(0xFF65A30D), Color(0xFF4D7C0F)),
    outlineColor: Color = Color(0x802D5A0E),
    corner: Dp = 14.dp,
    elevation: Dp = 6.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.6f)
            .shadow(elevation, shape)
            .clip(shape)
            .background(Brush.verticalGradient(gradient))
            .candyGloss(corner = corner, inset = 2.dp)
            .specularGlint(
                offsetX = if (corner > 16.dp) 14.dp else 10.dp,
                offsetY = if (corner > 16.dp) 6.dp else 5.dp,
                scale = if (corner > 16.dp) 1.25f else 1f
            )
            .border(1.dp, outlineColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
