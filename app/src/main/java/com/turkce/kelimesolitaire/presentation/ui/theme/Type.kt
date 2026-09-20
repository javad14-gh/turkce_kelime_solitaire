package com.turkce.kelimesolitaire.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R

val VazirmatnFont = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn, FontWeight.Medium),
    Font(R.font.vazirmatn, FontWeight.Bold),
    Font(R.font.vazirmatn, FontWeight.Black)
)

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
        textDirection = TextDirection.ContentOrRtl
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.ContentOrRtl
    ),
    labelLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        textDirection = TextDirection.ContentOrRtl
    )
)

