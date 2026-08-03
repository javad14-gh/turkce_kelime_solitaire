package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HintIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Lightbulb Outer Glow / Bulb
        val bulbPath = Path().apply {
            moveTo(w * 0.3f, h * 0.65f)
            cubicTo(w * 0.1f, h * 0.45f, w * 0.2f, h * 0.1f, w * 0.5f, h * 0.1f)
            cubicTo(w * 0.8f, h * 0.1f, w * 0.9f, h * 0.45f, w * 0.7f, h * 0.65f)
            lineTo(w * 0.65f, h * 0.75f)
            lineTo(w * 0.35f, h * 0.75f)
            close()
        }

        // Bulb Glow Fill
        drawPath(
            path = bulbPath,
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308)),
                center = Offset(w * 0.5f, h * 0.4f)
            )
        )

        // Bulb Outline
        drawPath(
            path = bulbPath,
            color = Color(0xFFFEF9C3),
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Bulb Metallic Base Screws
        drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(w * 0.36f, h * 0.76f),
            size = Size(w * 0.28f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(w * 0.4f, h * 0.89f),
            size = Size(w * 0.2f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
    }
}

@Composable
fun UndoIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val arrowPath = Path().apply {
            // Curved return path
            moveTo(w * 0.75f, h * 0.75f)
            cubicTo(w * 0.85f, h * 0.4f, w * 0.65f, h * 0.25f, w * 0.45f, h * 0.25f)
            lineTo(w * 0.3f, h * 0.25f)
        }

        drawPath(
            path = arrowPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Arrowhead
        val headPath = Path().apply {
            moveTo(w * 0.38f, h * 0.12f)
            lineTo(w * 0.2f, h * 0.25f)
            lineTo(w * 0.38f, h * 0.38f)
        }

        drawPath(
            path = headPath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun JokerIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Card Frame
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
            ),
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
        )

        drawRoundRect(
            color = Color(0xFFA855F7),
            topLeft = Offset(w * 0.15f, h * 0.1f),
            size = Size(w * 0.7f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.1f, w * 0.1f),
            style = Stroke(width = w * 0.06f)
        )

        // Star Emblem
        val starPath = Path().apply {
            val cx = w * 0.5f
            val cy = h * 0.5f
            val outer = w * 0.22f
            val inner = w * 0.09f
            for (i in 0..9) {
                val r = if (i % 2 == 0) outer else inner
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val x = cx + (r * Math.cos(angle)).toFloat()
                val y = cy + (r * Math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawPath(
            path = starPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFACC15), Color(0xFFEAB308))
            )
        )
    }
}

@Composable
fun AdIcon(
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // TV / Video Frame
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
            ),
            topLeft = Offset(w * 0.08f, h * 0.15f),
            size = Size(w * 0.84f, h * 0.7f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )

        // Play Triangle
        val playPath = Path().apply {
            moveTo(w * 0.4f, h * 0.35f)
            lineTo(w * 0.68f, h * 0.5f)
            lineTo(w * 0.4f, h * 0.65f)
            close()
        }

        drawPath(
            path = playPath,
            color = Color.White
        )
    }
}
