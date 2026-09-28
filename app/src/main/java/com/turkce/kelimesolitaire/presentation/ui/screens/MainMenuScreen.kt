package com.turkce.kelimesolitaire.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.turkce.kelimesolitaire.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardManager
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardState
import com.turkce.kelimesolitaire.presentation.ui.components.StarterPackDialog
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupHeaderBg
import com.turkce.kelimesolitaire.presentation.ui.theme.SoundVibrationBoxBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
import com.turkce.kelimesolitaire.presentation.ui.theme.PlayButtonFaceTop
import com.turkce.kelimesolitaire.presentation.ui.theme.PlayButtonFaceMid
import com.turkce.kelimesolitaire.presentation.ui.theme.PlayButtonFaceBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.PlayButtonFaceBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.PlayButtonShadow
import com.turkce.kelimesolitaire.presentation.ui.theme.getDifficultyRimColors
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

@Suppress("UNUSED_PARAMETER")
@Composable
fun MainMenuScreen(
    levelNumber: Int,
    coins: Int,
    completedLevels: Set<Int>,
    isAdFree: Boolean = false,
    isStarterPackPurchased: Boolean = false,
    hasUnclaimedDailyReward: Boolean = false,
    onStartGameClicked: (Int) -> Unit,
    onWatchAdForCoins: () -> Unit,
    onOpenStore: () -> Unit = {},
    onOpenDailyReward: () -> Unit = {},
    onPurchaseSku: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()
    var isPlayClicked by remember { mutableStateOf(false) }
    var showStarterPackDialog by remember { mutableStateOf(false) }

    // Calculate last unsolved level dynamically
    val lastUnsolvedLevel = remember(completedLevels) {
        var lvl = 1
        while (completedLevels.contains(lvl)) {
            lvl++
        }
        lvl
    }

    // Determine difficulty of the last unsolved level
    val difficulty = remember(lastUnsolvedLevel) {
        com.turkce.kelimesolitaire.domain.LevelGenerator.getDifficultyForLevel(lastUnsolvedLevel)
    }
    val difficultyRim = remember(difficulty) {
        getDifficultyRimColors(difficulty)
    }

    // Starter pack eligibility: only after level 3 is completed and not yet purchased
    val isEligibleForStarterPack = remember(isStarterPackPurchased, lastUnsolvedLevel, completedLevels) {
        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isStarterPackEligible(
            isStarterPackPurchased,
            lastUnsolvedLevel,
            completedLevels
        )
    }

    // Automatic Starter Pack popup: first time after completing level 3, or on periodic schedule
    androidx.compose.runtime.LaunchedEffect(isEligibleForStarterPack) {
        if (com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.shouldTriggerStarterPackPopup(
                context,
                isStarterPackPurchased,
                lastUnsolvedLevel,
                completedLevels
            )
        ) {
            showStarterPackDialog = true
            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.recordStarterPackPopupShown(
                context,
                lastUnsolvedLevel
            )
        }
    }

    var showSettingsMenu by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showThemeDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isSoundEnabled by remember { androidx.compose.runtime.mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isSoundEnabled(context)) }
    var isHapticEnabled by remember { androidx.compose.runtime.mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isHapticEnabled(context)) }

    val dailyRewardState = remember(hasUnclaimedDailyReward) {
        DailyRewardManager.getDailyRewardState(context)
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
            
            // Top Section: Top Bar HUD (Coins Left, Settings Right) + 7-Day Daily Reward Progress Bar Centered
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Coins Display: 3D Coin on the outside, box extending to the right (like menu buttons)
                        Box(
                            modifier = Modifier
                                .clickable { onOpenStore() },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // 3D Pill extending rightward from behind the coin (height 40dp matching settings button)
                            Box(
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .height(40.dp)
                                    .shadow(5.dp, RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp, topStart = 7.dp, bottomStart = 7.dp))
                                    .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp, topStart = 7.dp, bottomStart = 7.dp))
                                    .background(Color(0xFF6B5196))
                                    .padding(bottom = 3.dp)
                                    .clip(RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp, topStart = 5.dp, bottomStart = 5.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFD4C7EE),
                                                Color(0xFFBAA6DD),
                                                Color(0xFFA58ED0)
                                            )
                                        )
                                    )
                                    .padding(start = 36.dp, end = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedText(
                                    text = LocaleHelper.formatNumber(coins, isPersian),
                                    textColor = Color.White,
                                    outlineColor = Color(0xFF6B5196),
                                    outlineWidth = 8f,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // 3D Coin Icon on the outside, overlapping on the left
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .zIndex(2f),
                                contentAlignment = Alignment.Center
                            ) {
                                com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 54.dp)
                            }
                        }

                        // 2. Settings Menu Icon (Right) - 3D Button like Sound & Vibration (size 40dp, gear 34dp)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(5.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6B5196))
                                .padding(bottom = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFD4C7EE),
                                            Color(0xFFBAA6DD),
                                            Color(0xFFA58ED0)
                                        )
                                    )
                                )
                                .clickable {
                                    showSettingsMenu = true
                                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.setting),
                                contentDescription = "Settings",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Line 2: Centered 7-Day Daily Reward Progress Bar
                DailyReward7DayProgressBar(
                    state = dailyRewardState,
                    isPersian = isPersian,
                    hasUnclaimedReward = hasUnclaimedDailyReward,
                    onClick = { onOpenDailyReward() }
                )
            }

            // Central Game Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isPersian) {
                    Image(
                        painter = painterResource(id = R.drawable.title_logo),
                        contentDescription = "پاسور کلمات",
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .heightIn(max = 95.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    OutlinedText(
                        text = "TÜRKÇE KELİME",
                        textColor = SecondaryNeon,
                        outlineColor = Color(0xFF0F172A),
                        outlineWidth = 6f,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )
                    OutlinedText(
                        text = "SOLİTAİRE",
                        textColor = PrimaryNeon,
                        outlineColor = Color(0xFF0F172A),
                        outlineWidth = 6f,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3D Juicy Play Button with Difficulty Ribbon Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = 38.dp, bottom = 12.dp)
                ) {
                    // Elongated & Enlarged 3D Play Button Shell (rim themed dynamically with difficulty)
                    Box(
                        modifier = Modifier
                            .widthIn(min = 224.dp)
                            .shadow(16.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = difficultyRim.gradient
                                )
                            ) // 3D outer rim shell coordinated with level difficulty
                            .border(1.5.dp, difficultyRim.border, RoundedCornerShape(22.dp))
                            .padding(4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(PlayButtonShadow) // Dark 3D bottom base shadow
                            .padding(bottom = 5.5.dp) // Creates thick 3D bottom bevel
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PlayButtonFaceTop,
                                        PlayButtonFaceMid,
                                        PlayButtonFaceBottom
                                    )
                                )
                            )
                            .border(1.2.dp, PlayButtonFaceBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isPlayClicked) {
                                if (!isPlayClicked) {
                                    isPlayClicked = true
                                    onStartGameClicked(lastUnsolvedLevel)
                                }
                            }
                            .padding(horizontal = 40.dp, vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.levelTitle(lastUnsolvedLevel, isPersian),
                            textColor = Color.White,
                            outlineColor = PlayButtonShadow,
                            outlineWidth = 10f,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Overlapping Difficulty Ribbon Banner (Shown ONLY for Zor and CokZor levels, enlarged size)
                    if (difficulty == "Zor" || difficulty == "CokZor") {
                        val difficultyText = LocaleHelper.difficulty(difficulty, isPersian)
                        val ribbonColor = if (difficulty == "CokZor") Color(0xFFDC2626) else Color(0xFFEA580C)

                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(ribbonColor)
                                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 26.dp, vertical = 5.dp)
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
            }

            // Footer Section: Ad Banner and Policy Hooks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdBannerPlaceholder(isAdFree = isAdFree)
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocaleHelper.privacyPolicy(isPersian),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.privacy_policy_url)))
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                    Text(
                        text = "•",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = if (isPersian) "امنیت داده‌ها" else "Veri Güvenliği",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.data_safety_url)))
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                }
            }
        }

        // SETTINGS OVERLAY DIALOG FOR MAIN MENU (MATCHING REFERENCE UI SCREENSHOT)
        if (showSettingsMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(200f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { showSettingsMenu = false },
                contentAlignment = Alignment.Center
            ) {
                // Outer Dark 3D Frame
                Box(
                    modifier = Modifier
                        .width(330.dp)
                        .shadow(24.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF26005A))
                        .padding(bottom = 7.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8B35FA),
                                    Color(0xFF6414CE),
                                    DarkBg,
                                    Color(0xFF380084)
                                )
                            )
                        )
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Outer Header Bar (Title + Close Button)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedText(
                                text = LocaleHelper.settingsTitle(isPersian),
                                textColor = Color.White,
                                outlineColor = Color(0xFF190D69),
                                outlineWidth = 12.5f,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            // Top Right Close Button (cancel.png)
                            Image(
                                painter = painterResource(id = R.drawable.cancel),
                                contentDescription = "Close",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(40.dp)
                                    .clickable { showSettingsMenu = false }
                            )
                        }

                        // Inner Light 3D Tray (کادر داخلی روشن کمی کوچک‌تر دکمه‌ها)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                                .shadow(10.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF8F76BE))
                                .padding(bottom = 5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFEADBFC),
                                            PopupBg,
                                            Color(0xFFC7B6E4)
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 490.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 16.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Sound & Haptic Row (Two Square 84dp x 84dp 3D Buttons in BAA6DD style)
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.72f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                            // 1. Sound Speaker Toggle (Square 84dp x 84dp 3D Button)
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .shadow(10.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF6B5196))
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFD4C7EE),
                                                Color(0xFFBAA6DD),
                                                Color(0xFFA58ED0)
                                            )
                                        )
                                    )
                                    .clickable {
                                        val next = !isSoundEnabled
                                        isSoundEnabled = next
                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setSoundEnabled(context, next)
                                        if (next) {
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                        }
                                    }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sound),
                                    contentDescription = "Sound",
                                    modifier = Modifier.size(60.dp),
                                    colorFilter = if (!isSoundEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isSoundEnabled) 1f else 0.4f
                                )
                            }

                            // 2. Haptic Vibration Toggle (Square 84dp x 84dp 3D Button)
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .shadow(10.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF6B5196))
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFD4C7EE),
                                                Color(0xFFBAA6DD),
                                                Color(0xFFA58ED0)
                                            )
                                        )
                                    )
                                    .clickable {
                                        val next = !isHapticEnabled
                                        isHapticEnabled = next
                                        com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setHapticEnabled(context, next)
                                        if (next) {
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateLight(context)
                                        }
                                    }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.vibrate),
                                    contentDescription = "Vibration",
                                    modifier = Modifier.size(60.dp),
                                    colorFilter = if (!isHapticEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isHapticEnabled) 1f else 0.4f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                         // Action Buttons Column (All buttons 65% width of popup, +30% taller height, border DarkBg, 2dp internal padding, 60dp icons)
                         Column(
                             modifier = Modifier.fillMaxWidth(),
                             verticalArrangement = Arrangement.spacedBy(12.dp),
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             // 1. Theme Button (Opens separate theme details dialog)
                             Box(
                                 modifier = Modifier
                                     .fillMaxWidth(0.72f)
                                     .heightIn(min = 84.dp)
                                     .shadow(10.dp, RoundedCornerShape(20.dp))
                                     .clip(RoundedCornerShape(20.dp))
                                     .background(Color(0xFF6B5196))
                                     .padding(bottom = 4.dp)
                                     .clip(RoundedCornerShape(17.dp))
                                     .background(
                                         Brush.verticalGradient(
                                             colors = listOf(
                                                 Color(0xFFD4C7EE),
                                                 Color(0xFFBAA6DD),
                                                 Color(0xFFA58ED0)
                                             )
                                         )
                                     )
                                     .clickable { showThemeDialog = true }
                                     .padding(2.dp),
                                 contentAlignment = if (isPersian) Alignment.CenterEnd else Alignment.Center
                             ) {
                                 Row(
                                     modifier = if (isPersian) Modifier.padding(end = 6.dp) else Modifier,
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = if (isPersian) Arrangement.End else Arrangement.Center
                                 ) {
                                     if (isPersian) {
                                         OutlinedText(
                                             text = "رنگ‌بندی بازی",
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 25.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text(
                                             text = "🎨",
                                             fontSize = 45.sp
                                         )
                                     } else {
                                         Text(
                                             text = "🎨",
                                             fontSize = 45.sp
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         OutlinedText(
                                             text = "Oyun Teması",
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 24.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                     }
                                 }
                             }

                             // 2. Open Store Pill (BAA6DD, 57dp icon, 72% width)
                             Box(
                                 modifier = Modifier
                                     .fillMaxWidth(0.72f)
                                     .heightIn(min = 84.dp)
                                     .shadow(10.dp, RoundedCornerShape(20.dp))
                                     .clip(RoundedCornerShape(20.dp))
                                     .background(Color(0xFF6B5196))
                                     .padding(bottom = 4.dp)
                                     .clip(RoundedCornerShape(17.dp))
                                     .background(
                                         Brush.verticalGradient(
                                             colors = listOf(
                                                 Color(0xFFD4C7EE),
                                                 Color(0xFFBAA6DD),
                                                 Color(0xFFA58ED0)
                                             )
                                         )
                                     )
                                     .clickable {
                                         showSettingsMenu = false
                                         onOpenStore()
                                     }
                                     .padding(2.dp),
                                 contentAlignment = if (isPersian) Alignment.CenterEnd else Alignment.Center
                             ) {
                                 Row(
                                     modifier = if (isPersian) Modifier.padding(end = 6.dp) else Modifier,
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = if (isPersian) Arrangement.End else Arrangement.Center
                                 ) {
                                     if (isPersian) {
                                         OutlinedText(
                                             text = LocaleHelper.storeTitle(isPersian),
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 27.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Image(
                                             painter = painterResource(id = R.drawable.buy),
                                             contentDescription = "Store",
                                             modifier = Modifier.size(57.dp)
                                         )
                                     } else {
                                         Image(
                                             painter = painterResource(id = R.drawable.buy),
                                             contentDescription = "Store",
                                             modifier = Modifier.size(57.dp)
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         OutlinedText(
                                             text = LocaleHelper.storeTitle(isPersian),
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 27.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                     }
                                 }
                             }

                             // 3. Privacy Policy Pill (BAA6DD, 57dp icon, 72% width)
                             Box(
                                 modifier = Modifier
                                     .fillMaxWidth(0.72f)
                                     .heightIn(min = 84.dp)
                                     .shadow(10.dp, RoundedCornerShape(20.dp))
                                     .clip(RoundedCornerShape(20.dp))
                                     .background(Color(0xFF6B5196))
                                     .padding(bottom = 4.dp)
                                     .clip(RoundedCornerShape(17.dp))
                                     .background(
                                         Brush.verticalGradient(
                                             colors = listOf(
                                                 Color(0xFFD4C7EE),
                                                 Color(0xFFBAA6DD),
                                                 Color(0xFFA58ED0)
                                             )
                                         )
                                     )
                                     .clickable {
                                         showSettingsMenu = false
                                         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.privacy_policy_url)))
                                         context.startActivity(intent)
                                     }
                                     .padding(2.dp),
                                 contentAlignment = if (isPersian) Alignment.CenterEnd else Alignment.Center
                             ) {
                                 Row(
                                     modifier = if (isPersian) Modifier.padding(end = 6.dp) else Modifier,
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = if (isPersian) Arrangement.End else Arrangement.Center
                                 ) {
                                     if (isPersian) {
                                         OutlinedText(
                                             text = LocaleHelper.privacyPolicy(isPersian),
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 24.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Image(
                                             painter = painterResource(id = R.drawable.shield),
                                             contentDescription = "Privacy Policy",
                                             modifier = Modifier.size(57.dp)
                                         )
                                     } else {
                                         Image(
                                             painter = painterResource(id = R.drawable.shield),
                                             contentDescription = "Privacy Policy",
                                             modifier = Modifier.size(57.dp)
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         OutlinedText(
                                             text = LocaleHelper.privacyPolicy(isPersian),
                                             textColor = Color.White,
                                             outlineColor = Color(0xFF6B5196),
                                             outlineWidth = 12.5f,
                                             fontSize = 24.sp,
                                             fontWeight = FontWeight.Black
                                         )
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 }

        // SEPARATE THEME SELECTION MODAL DIALOG
        if (showThemeDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(260f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { showThemeDialog = false },
                contentAlignment = Alignment.Center
            ) {
                // Outer Dark 3D Frame
                Box(
                    modifier = Modifier
                        .width(330.dp)
                        .shadow(24.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF26005A))
                        .padding(bottom = 7.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8B35FA),
                                    Color(0xFF6414CE),
                                    DarkBg,
                                    Color(0xFF380084)
                                )
                            )
                        )
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Outer Header Bar (Title + Close Button)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedText(
                                text = if (isPersian) "انتخاب رنگ‌بندی بازی" else "Tema Seçimi",
                                textColor = Color.White,
                                outlineColor = Color(0xFF190D69),
                                outlineWidth = 12.5f,
                                fontSize = if (isPersian) 24.sp else 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            // Close Button
                            Image(
                                painter = painterResource(id = R.drawable.cancel),
                                contentDescription = "Close",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(38.dp)
                                    .clickable { showThemeDialog = false }
                            )
                        }

                        // Inner Light 3D Tray (کادر داخلی روشن کمی کوچک‌تر دکمه‌ها)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                                .shadow(10.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF8F76BE))
                                .padding(bottom = 5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFEADBFC),
                                            PopupBg,
                                            Color(0xFFC7B6E4)
                                        )
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                com.turkce.kelimesolitaire.presentation.ui.components.ThemeSelectorSection(
                                    isPersian = isPersian,
                                    fontFamily = nunitoFont,
                                    onThemeSelected = {
                                        showThemeDialog = false
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


        // STARTER PACK SPECIAL OFFER MODAL DIALOG
        if (showStarterPackDialog && isEligibleForStarterPack) {
            StarterPackDialog(
                onDismiss = { showStarterPackDialog = false },
                onBuy = {
                    onPurchaseSku(MyketBillingConfig.SKU_STARTER_PACK)
                    showStarterPackDialog = false
                }
            )
        }
    }
}

/**
 * Floating 3D Side Offer Badge for Starter Pack, matching casual game standards (Royal Match, Candy Crush).
 */
@Composable
fun StarterPackSideButton(
    isPersian: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nunitoFont = rememberNunitoFont()
    val infiniteTransition = rememberInfiniteTransition(label = "sideOfferPulse")

    // Gentle floating pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            // 3D Outer Gold Rim
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309))
                )
            )
            .border(1.5.dp, Color(0xFFFEF08A), RoundedCornerShape(18.dp))
            .padding(2.5.dp)
            // 3D Bottom Bevel
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF4C0519)) // Deep ruby-crimson bevel base
            .padding(bottom = 3.5.dp)
            // Button Face: Rich Royal Ruby/Crimson Gradient
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFB7185), // Soft rose top highlight
                        Color(0xFFE11D48), // Vibrant crimson
                        Color(0xFF881337)  // Deep rich wine base
                    )
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 7.dp)
            .width(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gift icon with discount tag overlay
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.size(42.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gift),
                    contentDescription = "Starter Pack Offer",
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.Center)
                )

                // Discount -70% badge
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDC2626))
                        .border(1.dp, Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = if (isPersian) "٪۷۰" else "-70%",
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Mini Gold Banner label at bottom
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                        )
                    )
                    .border(0.8.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (isPersian) "بسته ویژه" else "ÖZEL",
                    color = Color(0xFF451A03),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DailyReward7DayProgressBar(
    state: DailyRewardState,
    isPersian: Boolean,
    hasUnclaimedReward: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appFont = rememberAppFont()

    // Calculate week streak status (1..7)
    val claimedThisWeek = remember(state.claimedDaysCount, state.isReadyToClaimToday) {
        if (!state.isReadyToClaimToday && state.claimedDaysCount > 0 && state.claimedDaysCount % 7 == 0) {
            7
        } else {
            state.claimedDaysCount % 7
        }
    }
    val todayReadyDay = remember(claimedThisWeek, state.isReadyToClaimToday) {
        if (state.isReadyToClaimToday) (claimedThisWeek + 1).coerceIn(1, 7) else 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "daily_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Outer Center Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .widthIn(max = 330.dp),
        contentAlignment = Alignment.Center
    ) {
        // 3D Plump Daily Reward Box (با رنگ پس‌زمینه دکمه‌های توی منو)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF6B5196))
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD4C7EE),
                            Color(0xFFBAA6DD),
                            Color(0xFFA58ED0)
                        )
                    )
                )
                .then(
                    if (hasUnclaimedReward) {
                        Modifier.border(
                            1.8.dp,
                            Color(0xFFFFD700).copy(alpha = glowAlpha),
                            RoundedCornerShape(15.dp)
                        )
                    } else Modifier
                )
                .clickable { onClick() }
        ) {
            // 7 Days Progress Track inside 3D Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (day in 1..7) {
                    val isClaimed = day <= claimedThisWeek
                    val isReady = day == todayReadyDay
                    val isDay7 = day == 7

                    if (isDay7) {
                        // Day 7: Chest
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .then(if (isReady) Modifier.scale(pulseScale) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chest),
                                contentDescription = "Day 7 Chest",
                                modifier = Modifier.size(38.dp),
                                alpha = if (isClaimed) 0.55f else 1f
                            )
                            if (isClaimed) {
                                Image(
                                    painter = painterResource(id = R.drawable.tick),
                                    contentDescription = "Claimed",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        // Days 1 to 6: Step Nodes
                        if (isClaimed) {
                            Image(
                                painter = painterResource(id = R.drawable.tick),
                                contentDescription = "Claimed",
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (isReady) Modifier.scale(pulseScale) else Modifier)
                                    .clip(CircleShape)
                                    .background(
                                        if (isReady)
                                            Brush.verticalGradient(listOf(Color(0xFFFFE066), Color(0xFFF59E0B)))
                                        else
                                            Brush.verticalGradient(listOf(Color(0xFF7E65A8), Color(0xFF5A4184)))
                                    )
                                    .border(
                                        width = if (isReady) 1.5.dp else 1.dp,
                                        color = if (isReady) Color.White else Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isReady) {
                                    Text(
                                        text = LocaleHelper.formatNumber(day, isPersian),
                                        color = Color(0xFF1E293B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = appFont
                                    )
                                } else {
                                    Text(
                                        text = LocaleHelper.formatNumber(day, isPersian),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = appFont
                                    )
                                }
                            }
                        }
                    }

                    // Connecting Line to next node
                    if (day < 7) {
                        val isLineActive = day < claimedThisWeek || (day == claimedThisWeek && todayReadyDay == day + 1)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (isLineActive)
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF16A34A), Color(0xFFFFD700))
                                        )
                                    else
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF8F76BE), Color(0xFF7A60A8))
                                        )
                                )
                        )
                    }
                }
            }
        }
    }
}


