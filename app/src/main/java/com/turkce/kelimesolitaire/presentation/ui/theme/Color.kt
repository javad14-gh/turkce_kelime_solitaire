package com.turkce.kelimesolitaire.presentation.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBg = Color(0xFF0D0921)
val DarkCard = Color(0xFF1B123B)
val PrimaryNeon = Color(0xFF8B5CF6)
val SecondaryNeon = Color(0xFF06B6D4)
val AccentGold = Color(0xFFFBBF24)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)

// Neutral colors for UI elements
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFFD1D5DB)
val BorderGlass = Color(0xFF3B2E6E)
val CardHighlight = Color(0xFF4C3E8A)

/**
 * 3D Button outer rim colors coordinated with level difficulty
 */
data class DifficultyRimColors(
    val gradient: List<Color>,
    val border: Color,
    val baseShadow: Color
)

fun getDifficultyRimColors(difficulty: String): DifficultyRimColors {
    return when (difficulty) {
        "Kolay" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE8FDF0), Color(0xFF4ADE80), Color(0xFF16A34A)),
            border = Color(0xFF86EFAC),
            baseShadow = Color(0xFF14532D)
        )
        "Orta" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            border = Color(0xFF7DD3FC),
            baseShadow = Color(0xFF075985)
        )
        "Zor" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFFFEDD5), Color(0xFFFB923C), Color(0xFFEA580C)),
            border = Color(0xFFFDBA74),
            baseShadow = Color(0xFF9A3412)
        )
        "CokZor" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFFEE2E2), Color(0xFFF43F5E), Color(0xFFDC2626)),
            border = Color(0xFFFDA4AF),
            baseShadow = Color(0xFF881337)
        )
        else -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            border = Color(0xFF7DD3FC),
            baseShadow = Color(0xFF075985)
        )
    }
}
