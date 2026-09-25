package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.SuccessGreen
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.turkce.kelimesolitaire.domain.LevelGenerator
import com.turkce.kelimesolitaire.presentation.ui.components.ConfettiPartyPopper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

@Composable
fun LevelCompleteScreen(
    levelNumber: Int,
    bonusCoins: Int,
    isRewardDoubled: Boolean = false,
    onDoubleRewardClicked: () -> Unit = {},
    onNextLevelClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    isAdFree: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()
    var isActionTriggered by remember { mutableStateOf(false) }

    // Upcoming level difficulty determination
    val nextLevel = levelNumber + 1
    val nextDifficulty = remember(nextLevel) {
        LevelGenerator.getDifficultyForLevel(nextLevel)
    }

    // Celebratory victory pop entrance animation
    val entranceScale = remember { Animatable(0.2f) }
    val entranceAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            entranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(350, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Victory Title with celebratory pop entrance & golden halo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = entranceScale.value
                    scaleY = entranceScale.value
                    alpha = entranceAlpha.value
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Pulsing golden aura behind trophy
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x66F59E0B),
                                        Color(0x00F59E0B)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.trophy),
                        contentDescription = "Trophy",
                        modifier = Modifier.size(92.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedText(
                    text = LocaleHelper.victoryTitle(isPersian),
                    textColor = SuccessGreen,
                    outlineColor = Color(0xFF0F172A),
                    outlineWidth = 6f,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LocaleHelper.victorySubtitle(levelNumber, isPersian),
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }

            // Reward Summary Card
            Box(
                modifier = Modifier
                    .width(290.dp)
                    .background(DarkCard, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPersian) "پاداش دریافتی" else "KAZANILAN ÖDÜL",
                        color = Color(0xFF94A3B8),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 40.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        val currentCoinsDisplay = if (isRewardDoubled) bonusCoins * 2 else bonusCoins
                        Text(
                            text = "+${LocaleHelper.formatNumber(currentCoinsDisplay, isPersian)} ${if (isPersian) "سکه" else "Altın"}",
                            color = AccentGold,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }
                }
            }

            // 2X Reward Doubler Button with Rewarded Ad
            if (bonusCoins > 0) {
                if (!isRewardDoubled) {
                    Box(
                        modifier = Modifier
                            .shadow(12.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE))
                                )
                            )
                            .padding(2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF31104D))
                            .padding(bottom = 3.5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF7C3AED), // Royal Purple
                                        Color(0xFFD97706)  // Vibrant Amber Gold
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                            .clickable {
                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                onDoubleRewardClicked()
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.tv),
                                contentDescription = "Watch Ad",
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${LocaleHelper.doubleReward(isPersian)} (+${LocaleHelper.formatNumber(bonusCoins, isPersian)} 🪙)",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x3310B981))
                            .border(1.5.dp, Color(0xFF34D399), RoundedCornerShape(14.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LocaleHelper.rewardDoubled(isPersian),
                            color = Color(0xFF34D399),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = nunitoFont
                        )
                    }
                }
            }

            // Navigation Actions with 3D Tactile Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 3D Next Level Button with Overlapping Difficulty Ribbon Banner
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(14.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            )
                            .padding(2.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E3A07))
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF84CC16),
                                        Color(0xFF65A30D),
                                        Color(0xFF4D7C0F)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFBEF264), RoundedCornerShape(18.dp))
                            .clickable(enabled = !isActionTriggered) {
                                if (!isActionTriggered) {
                                    isActionTriggered = true
                                    onNextLevelClicked()
                                }
                            }
                            .padding(horizontal = 30.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.nextLevel(isPersian),
                            textColor = Color.White,
                            outlineColor = Color(0xFF1E3A07),
                            outlineWidth = 5f,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Overlapping Difficulty Ribbon Banner (Shown for Zor and CokZor levels, exactly like MainMenuScreen)
                    if (nextDifficulty == "Zor" || nextDifficulty == "CokZor") {
                        val difficultyText = LocaleHelper.difficulty(nextDifficulty, isPersian)
                        val ribbonColor = if (nextDifficulty == "CokZor") Color(0xFFDC2626) else Color(0xFFEA580C)

                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(ribbonColor)
                                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 24.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = difficultyText,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Main Menu Button (Slate Glass)
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF64748B), Color(0xFF334155))
                            )
                        )
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(bottom = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF334155),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .clickable(enabled = !isActionTriggered) {
                            if (!isActionTriggered) {
                                isActionTriggered = true
                                onMainMenuClicked()
                            }
                        }
                        .padding(horizontal = 26.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LocaleHelper.mainMenu(isPersian),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Ad Banner Footer
            AdBannerPlaceholder(isAdFree = isAdFree)
        }

        // Celebratory Party Popper Confetti Shower
        ConfettiPartyPopper()
    }
}
