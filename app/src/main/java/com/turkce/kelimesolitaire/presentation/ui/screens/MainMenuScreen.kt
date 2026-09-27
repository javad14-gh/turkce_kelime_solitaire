package com.turkce.kelimesolitaire.presentation.ui.screens

import android.content.Intent
import android.net.Uri
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
                        // 1. Coins Display: 3D Coin on the outside, box extending to the right
                        Box(
                            modifier = Modifier
                                .clickable { onOpenStore() },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Pill / Box extending rightward from behind the coin
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp, topStart = 4.dp, bottomStart = 4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF1E293B).copy(alpha = 0.95f),
                                                Color(0xFF0F172A).copy(alpha = 0.98f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.2.dp,
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFD700).copy(alpha = 0.7f),
                                                Color.White.copy(alpha = 0.2f)
                                            )
                                        ),
                                        RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp, topStart = 4.dp, bottomStart = 4.dp)
                                    )
                                    .padding(start = 24.dp, end = 12.dp)
                            ) {
                                Text(
                                    text = LocaleHelper.formatNumber(coins, isPersian),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont
                                )
                            }

                            // 3D Coin Icon on the outside, overlapping on the left
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .zIndex(2f),
                                contentAlignment = Alignment.Center
                            ) {
                                com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 42.dp)
                            }
                        }

                        // 2. Settings Menu Icon (Right)
                        Image(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Settings",
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { showSettingsMenu = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                    modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
                ) {
                    // Compact 3D Play Button Shell (rim themed dynamically with difficulty)
                    Box(
                        modifier = Modifier
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
                            .padding(bottom = 5.dp) // Creates thick 3D bottom bevel
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
                            .padding(horizontal = 30.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.levelTitle(lastUnsolvedLevel, isPersian),
                            textColor = Color.White,
                            outlineColor = Color(0xFF1E3A07),
                            outlineWidth = 5f,
                            fontSize = 32.sp,
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

                Spacer(modifier = Modifier.height(16.dp))

                // 3D Store Button on Main Screen (with LARGE buy.png icon extending to button edges)
                Box(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                            )
                        )
                        .padding(3.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0xFF78350F))
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B),
                                    Color(0xFFD97706),
                                    Color(0xFFB45309)
                                )
                            )
                        )
                        .clickable { onOpenStore() }
                        .height(58.dp)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.buy),
                            contentDescription = "Store",
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LocaleHelper.storeTitle(isPersian),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
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
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .shadow(20.dp, RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3826B4), Color(0xFF241584), Color(0xFF190D69))
                            )
                        )
                        .border(2.dp, Color(0xFF6366F1), RoundedCornerShape(26.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Arched Header Bar with Close (X) Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF4C38CE), Color(0xFF2C1990))
                                    )
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedText(
                                text = LocaleHelper.settingsTitle(isPersian),
                                textColor = Color.White,
                                outlineColor = Color(0xFF190D69),
                                outlineWidth = 5f,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            // Top Right Close Button (cancel.png)
                            Image(
                                painter = painterResource(id = R.drawable.cancel),
                                contentDescription = "Close",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(34.dp)
                                    .clickable { showSettingsMenu = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Sound & Haptic Toggle Outer Container Box
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF150A54).copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Sound Speaker Toggle
                                Image(
                                    painter = painterResource(id = R.drawable.sound),
                                    contentDescription = "Sound",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable {
                                            val next = !isSoundEnabled
                                            isSoundEnabled = next
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setSoundEnabled(context, next)
                                            if (next) {
                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                                            }
                                        },
                                    colorFilter = if (!isSoundEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isSoundEnabled) 1f else 0.4f
                                )

                                // 2. Haptic Vibration Toggle
                                Image(
                                    painter = painterResource(id = R.drawable.vibrate),
                                    contentDescription = "Vibration",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable {
                                            val next = !isHapticEnabled
                                            isHapticEnabled = next
                                            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.setHapticEnabled(context, next)
                                            if (next) {
                                                com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.vibrateLight(context)
                                            }
                                        },
                                    colorFilter = if (!isHapticEnabled) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                    alpha = if (isHapticEnabled) 1f else 0.4f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Capsule Pills Column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Privacy Policy Pill (Teal 3D Gradient)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showSettingsMenu = false
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(com.turkce.kelimesolitaire.R.string.privacy_policy_url)))
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.shield),
                                        contentDescription = "Privacy Policy",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = LocaleHelper.privacyPolicy(isPersian),
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }

                            // 2. Open Store Pill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .clickable {
                                        showSettingsMenu = false
                                        onOpenStore()
                                    }
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.buy),
                                        contentDescription = "Store",
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = LocaleHelper.storeTitle(isPersian),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = nunitoFont
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Side Offer Badge for Starter Pack (like top mobile casual games)
        if (isEligibleForStarterPack) {
            StarterPackSideButton(
                isPersian = isPersian,
                onClick = {
                    com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playButtonClickSound(context)
                    showStarterPackDialog = true
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = 20.dp)
            )
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

    // Outer Box: Centers the Ribbon Badge right on the top edge/rim of the card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .widthIn(max = 300.dp)
            .padding(top = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // The Main Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF131D31).copy(alpha = 0.94f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                if (hasUnclaimedReward)
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = glowAlpha),
                            Color(0xFF4ADE80).copy(alpha = glowAlpha)
                        )
                    )
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
            )
        ) {
            // 7 Days Progress Track inside card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (day in 1..7) {
                    val isClaimed = day <= claimedThisWeek
                    val isReady = day == todayReadyDay
                    val isDay7 = day == 7

                    if (isDay7) {
                        // Day 7: Large 42dp Chest (matching Settings icon size: 42dp)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .then(if (isReady) Modifier.scale(pulseScale) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chest),
                                contentDescription = "Day 7 Chest",
                                modifier = Modifier.size(42.dp),
                                alpha = if (isClaimed) 0.55f else 1f
                            )
                            if (isClaimed) {
                                Image(
                                    painter = painterResource(id = R.drawable.tick),
                                    contentDescription = "Claimed",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        // Days 1 to 6: Step Nodes (Ticks at 70% scale: 29.dp)
                        if (isClaimed) {
                            Image(
                                painter = painterResource(id = R.drawable.tick),
                                contentDescription = "Claimed",
                                modifier = Modifier.size(29.dp)
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
                                            Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                    )
                                    .border(
                                        width = if (isReady) 1.5.dp else 1.dp,
                                        color = if (isReady) Color.White else Color.White.copy(alpha = 0.2f),
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
                                        color = Color.White.copy(alpha = 0.5f),
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
                                .height(2.5.dp)
                                .background(
                                    if (isLineActive)
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF22C55E), Color(0xFFFFD700))
                                        )
                                    else
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                                        )
                                )
                        )
                    }
                }
            }
        }

        // Ribbon Badge Centered on Top Edge (Overlapping top rim like difficulty badge)
        Box(
            modifier = Modifier
                .offset(y = (-11).dp)
                .zIndex(3f)
                .then(if (hasUnclaimedReward) Modifier.scale(pulseScale) else Modifier)
                .shadow(5.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (hasUnclaimedReward)
                        Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C)))
                    else
                        Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                )
                .border(
                    1.2.dp,
                    if (hasUnclaimedReward) Color.White else Color.White.copy(alpha = 0.6f),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isPersian) {
                    if (hasUnclaimedReward) "جایزه روزانه (دریافت 🪙)" else "جایزه ورود روزانه"
                } else {
                    if (hasUnclaimedReward) "Günlük Ödül (Al 🪙)" else "Günlük Giriş Ödülü"
                },
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = appFont,
                letterSpacing = 0.4.sp
            )
        }
    }
}


