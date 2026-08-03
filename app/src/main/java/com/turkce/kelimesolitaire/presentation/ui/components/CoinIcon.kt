package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CoinIcon(
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val radius = width / 2f
        val center = Offset(width / 2f, height / 2f)

        // Outer Dark Shadow Rim
        drawCircle(
            color = Color(0xFF78350F),
            radius = radius,
            center = center
        )

        // Main 3D Gold Gradient Body
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFEF08A), // Top shiny highlight
                    Color(0xFFFACC15), // Mid vibrant gold
                    Color(0xFFCA8A04), // Dark gold base
                    Color(0xFF854D0E)  // 3D bottom bevel
                )
            ),
            radius = radius * 0.92f,
            center = center
        )

        // Inner Embossed Gold Ring
        drawCircle(
            color = Color(0xFFFEF9C3).copy(alpha = 0.9f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = width * 0.08f)
        )

        // Center Gold Core
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFDE047),
                    Color(0xFFEAB308)
                )
            ),
            radius = radius * 0.65f,
            center = center
        )

        // Top Gloss Highlight Spot
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = radius * 0.28f,
            center = Offset(width * 0.38f, height * 0.35f)
        )
    }
}
