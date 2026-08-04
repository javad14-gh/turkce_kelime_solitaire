package com.turkce.kelimesolitaire.presentation.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CoinIcon(
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val coinBitmap = remember(context) {
        try {
            context.assets.open("coin.png").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    if (coinBitmap != null) {
        Image(
            bitmap = coinBitmap,
            contentDescription = "Coin",
            modifier = modifier.size(size)
        )
    } else {
        // Fallback Vector Coin if asset cannot be loaded
        FallbackVectorCoin(size = size, modifier = modifier)
    }
}

@Composable
private fun FallbackVectorCoin(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val radius = width / 2f
        val center = Offset(width / 2f, height / 2f)

        drawCircle(color = Color(0xFF78350F), radius = radius, center = center)
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFEF08A), Color(0xFFFACC15), Color(0xFFCA8A04), Color(0xFF854D0E))
            ),
            radius = radius * 0.92f,
            center = center
        )
        drawCircle(
            color = Color(0xFFFEF9C3).copy(alpha = 0.9f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = width * 0.08f)
        )
        drawCircle(
            brush = Brush.verticalGradient(colors = listOf(Color(0xFFFDE047), Color(0xFFEAB308))),
            radius = radius * 0.65f,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = radius * 0.28f,
            center = Offset(width * 0.38f, height * 0.35f)
        )
    }
}
