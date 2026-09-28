package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.theme.GameThemeManager
import com.turkce.kelimesolitaire.presentation.ui.theme.ThemePreset
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager

@Composable
fun ThemeSelectorSection(
    isPersian: Boolean,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme = GameThemeManager.currentTheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(1.2.dp, com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section Title
            Text(
                text = if (isPersian) "🎨 تم و رنگ‌بندی بازی" else "🎨 Oyun Teması",
                color = com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets Horizontal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemePreset.values().forEach { preset ->
                    val isSelected = preset == currentTheme
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "themeScale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .scale(scale)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (preset != currentTheme) {
                                    GameThemeManager.setTheme(preset, context)
                                    GameSettingsManager.playButtonClickSound(context)
                                    GameSettingsManager.vibrateLight(context)
                                }
                            }
                    ) {
                        // 4-Color Palette Swatch Circle
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .shadow(
                                    elevation = if (isSelected) 8.dp else 2.dp,
                                    shape = CircleShape,
                                    ambientColor = if (isSelected) Color(0xFFFFD700) else Color.Black,
                                    spotColor = if (isSelected) Color(0xFFFFD700) else Color.Black
                                )
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw 4 color sectors: c1, c2, c3, c4
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawArc(preset.c1, startAngle = 180f, sweepAngle = 90f, useCenter = true)
                                drawArc(preset.c2, startAngle = 270f, sweepAngle = 90f, useCenter = true)
                                drawArc(preset.c4, startAngle = 0f, sweepAngle = 90f, useCenter = true)
                                drawArc(preset.c3, startAngle = 90f, sweepAngle = 90f, useCenter = true)
                            }

                            // Center Checkmark badge if selected
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF190D69),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Theme Name
                        Text(
                            text = if (isPersian) preset.titlePersian else preset.titleTurkish,
                            color = if (isSelected) com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg else com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontFamily = fontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
