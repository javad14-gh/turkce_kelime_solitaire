package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun rememberNunitoFont(): FontFamily {
    val context = LocalContext.current
    return remember(context) {
        try {
            FontFamily(Font(path = "fonts/nunito_black.ttf", assetManager = context.assets))
        } catch (e: Throwable) {
            FontFamily.Serif
        }
    }
}

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    outlineColor: Color = Color(0xFF1E1B4B),
    outlineWidth: Float = 6f,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Black,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    shadowColor: Color = Color.Black.copy(alpha = 0.5f),
    shadowOffsetY: Float = 4f
) {
    val nunitoFont = rememberNunitoFont()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Outline + Drop Shadow
        Text(
            text = text,
            color = outlineColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = nunitoFont,
            textAlign = textAlign,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing,
            style = TextStyle(
                drawStyle = Stroke(
                    width = outlineWidth,
                    join = StrokeJoin.Round
                ),
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset(0f, shadowOffsetY),
                    blurRadius = 6f
                )
            )
        )
        // 2. Foreground Solid Text
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = nunitoFont,
            textAlign = textAlign,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing
        )
    }
}
