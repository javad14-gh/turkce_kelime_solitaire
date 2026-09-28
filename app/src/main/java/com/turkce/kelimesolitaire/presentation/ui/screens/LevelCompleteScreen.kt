package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.domain.LevelGenerator
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.ConfettiPartyPopper
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.getDifficultyRimColors
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import kotlinx.coroutines.launch

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
    val nextDifficultyRim = remember(nextDifficulty) {
        getDifficultyRimColors(nextDifficulty)
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
            Spacer(modifier = Modifier.height(10.dp))

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
                            .size(120.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x77F59E0B),
                                        Color(0x22F59E0B),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.trophy),
                        contentDescription = "Trophy",
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedText(
                    text = LocaleHelper.victoryTitle(isPersian),
                    textColor = Color(0xFFFFD700),
                    outlineColor = Color(0xFF190D69),
                    outlineWidth = 6f,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = LocaleHelper.victorySubtitle(levelNumber, isPersian),
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }

            // Reward Summary Card (Matching Game Design System)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .shadow(20.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(PopupBg)
                    .border(
                        width = 2.5.dp,
                        color = PopupBorder,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(vertical = 18.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPersian) "پاداش مرحله" else "BÖLÜM ÖDÜLÜ",
                        color = DarkBg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coin Display Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.55f))
                            .border(1.2.dp, DarkBg.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CoinIcon(size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            val currentCoinsDisplay = if (isRewardDoubled) bonusCoins * 2 else bonusCoins
                            Text(
                                text = "+${LocaleHelper.formatNumber(currentCoinsDisplay, isPersian)} ${if (isPersian) "سکه" else "Altın"}",
                                color = DarkBg,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont
                            )
                        }
                    }

                    // 2X Reward Doubler Button with Rewarded Ad
                    if (bonusCoins > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        if (!isRewardDoubled) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF7C3AED), // Royal Purple
                                                Color(0xFFD97706)  // Vibrant Amber Gold
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .clickable {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onDoubleRewardClicked()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.tv),
                                        contentDescription = "Watch Ad",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${LocaleHelper.doubleReward(isPersian)} (+${LocaleHelper.formatNumber(bonusCoins, isPersian)} 🪙)",
                                        color = Color.White,
                                        fontSize = 15.sp,
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
                                    text = "✓ ${LocaleHelper.rewardDoubled(isPersian)}",
                                    color = Color(0xFF34D399),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = nunitoFont
                                )
                            }
                        }
                    }
                }
            }

            // Navigation Actions with 3D Tactile Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. 3D Next Level Button with Overlapping Difficulty Ribbon Banner
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .shadow(16.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = nextDifficultyRim.gradient
                                )
                            )
                            .border(1.5.dp, nextDifficultyRim.border, RoundedCornerShape(22.dp))
                            .padding(4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E3A07)) // Dark 3D bottom base shadow
                            .padding(bottom = 5.dp) // Creates thick 3D bottom bevel
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFA3E635), // Glossy top highlight green
                                        Color(0xFF65A30D), // Mid vibrant green
                                        Color(0xFF4D7C0F)  // Inner shade
                                    )
                                )
                            )
                            .border(1.2.dp, Color(0xFFBEF264).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isActionTriggered) {
                                if (!isActionTriggered) {
                                    isActionTriggered = true
                                    onNextLevelClicked()
                                }
                            }
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.nextLevel(isPersian),
                            textColor = Color.White,
                            outlineColor = Color(0xFF1E3A07),
                            outlineWidth = 5f,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Overlapping Difficulty Ribbon Banner (Shown for Zor and CokZor levels)
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

                Spacer(modifier = Modifier.height(12.dp))

                // 2. 3D Main Menu Button (3D Store structure, RED Exit Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(10.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkBg)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0xFF7F1D1D))
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFFB91C1C))
                            )
                        )
                        .clickable(enabled = !isActionTriggered) {
                            if (!isActionTriggered) {
                                isActionTriggered = true
                                onMainMenuClicked()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.exit),
                            contentDescription = "Main Menu",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocaleHelper.mainMenu(isPersian),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }
                }
            }

            // Ad Banner Footer
            AdBannerPlaceholder(isAdFree = isAdFree)
        }

        // Celebratory Party Popper Confetti Shower
        ConfettiPartyPopper()
    }
}
