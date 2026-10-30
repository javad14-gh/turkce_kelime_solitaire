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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.turkce.kelimesolitaire.R
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardManager
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardState
import com.turkce.kelimesolitaire.presentation.ui.components.StarterPackDialog
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.components.candyGloss
import com.turkce.kelimesolitaire.presentation.ui.components.specularGlint
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont
import com.turkce.kelimesolitaire.presentation.ui.theme.mainBg
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.ui.components.CoinCounter
import com.turkce.kelimesolitaire.presentation.ui.components.SettingsDialog
import com.turkce.kelimesolitaire.presentation.ui.components.TopBarIconButton


@Suppress("UNUSED_PARAMETER")
@Composable
fun MainMenuScreen(
    modifier: Modifier = Modifier,
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

    // Starter pack eligibility: only after level 3 is completed and not yet purchased
    val isEligibleForStarterPack =
        remember(isStarterPackPurchased, lastUnsolvedLevel, completedLevels) {
            GameSettingsManager.isStarterPackEligible(
                isStarterPackPurchased,
                lastUnsolvedLevel,
                completedLevels
            )
        }

    // Automatic Starter Pack popup: first time after completing level 3, or on periodic schedule
    androidx.compose.runtime.LaunchedEffect(isEligibleForStarterPack) {
        if (GameSettingsManager.shouldTriggerStarterPackPopup(
                context,
                isStarterPackPurchased,
                lastUnsolvedLevel,
                completedLevels
            )
        ) {
            showStarterPackDialog = true
            GameSettingsManager.recordStarterPackPopupShown(
                context,
                lastUnsolvedLevel
            )
        }
    }

    var showSettingsMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isSoundEnabled by remember {
        mutableStateOf(
            GameSettingsManager.isSoundEnabled(
                context
            )
        )
    }
    var isHapticEnabled by remember {
        mutableStateOf(
            GameSettingsManager.isHapticEnabled(
                context
            )
        )
    }

    val dailyRewardState = remember(hasUnclaimedDailyReward) {
        DailyRewardManager.getDailyRewardState(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mainBg)
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
                        CoinCounter(
                            coins = coins,
                            isPersian,
                            onClick = {onOpenStore()}
                        )

                        // 2. Settings Menu Icon (Right) - 3D Button like Sound & Vibration (size 40dp, gear 34dp)
                        TopBarIconButton(
                            iconRes = R.drawable.setting,
                            contentDescription = "Settings",
                            onClick = {
                                showSettingsMenu = true
                                GameSettingsManager.playButtonClickSound(
                                    context
                                )
                            }

                        )


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
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
            }

            // 3D Juicy Play Button with Difficulty Ribbon Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = 38.dp, bottom = 10.dp)
                ) {
                    val buttonShape = RoundedCornerShape(18.dp)
                    val buttonOutline = Color(0x802D5A0E) // سبز تیره دکمه خرید بسته

                    Box(
                        modifier = Modifier
                            .widthIn(min = 200.dp)
                            .shadow(8.dp, buttonShape)
                            .clip(buttonShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFA3E635),
                                        Color(0xFF65A30D),
                                        Color(0xFF4D7C0F)
                                    )
                                )
                            )
                            .candyGloss(corner = 18.dp, inset = 2.dp)
                            .specularGlint(offsetX = 14.dp, offsetY = 6.dp, scale = 1.3f)
                            .border(1.dp, buttonOutline, buttonShape)
                            .clickable(enabled = !isPlayClicked) {
                                if (!isPlayClicked) {
                                    isPlayClicked = true
                                    GameSettingsManager.playButtonClickSound(
                                        context
                                    )
                                    onStartGameClicked(lastUnsolvedLevel)
                                }
                            }
                            .padding(horizontal = 40.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedText(
                            text = LocaleHelper.levelTitle(lastUnsolvedLevel, isPersian),
                            textColor = Color.White,
                            outlineColor = buttonOutline,
                            outlineWidth = 8f,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            shadowColor = buttonOutline.copy(alpha = 0.6f),
                            shadowOffsetY = 3f
                        )
                    }

                    // Overlapping Difficulty Ribbon Banner (Shown ONLY for Zor and CokZor levels, enlarged size)
                    if (difficulty == "Zor" || difficulty == "CokZor") {
                        val difficultyText = LocaleHelper.difficulty(difficulty, isPersian)
                        val ribbonColor =
                            if (difficulty == "CokZor") Color(0xFFDC2626) else Color(0xFFEA580C)

                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(ribbonColor)
                                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 23.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = difficultyText,
                                color = Color.White,
                                fontSize = 13.sp,
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
            )
            {
                AdBannerPlaceholder(isAdFree = isAdFree)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocaleHelper.privacyPolicy(isPersian),
                        color = DarkMain.copy(0.5f),
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.privacy_policy_url))
                                )
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                    Text(
                        text = "•",
                        color = DarkMain.copy(0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = if (isPersian) "امنیت داده‌ها" else "Veri Güvenliği",
                        color = DarkMain.copy(0.5f),
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.data_safety_url))
                                )
                                context.startActivity(intent)
                            }
                            .padding(8.dp)
                    )
                }
            }
        }

        if (showSettingsMenu) {
            SettingsDialog(
                isPersian = isPersian,
                soundEnabled = isSoundEnabled,
                hapticEnabled = isHapticEnabled,
                onSoundChange = {
                    isSoundEnabled = it
                    GameSettingsManager.setSoundEnabled(context, it)
                },
                onHapticChange = {
                    isHapticEnabled = it
                    GameSettingsManager.setHapticEnabled(context, it)
                },
                onDismiss = { showSettingsMenu = false },
                onOpenTheme = { showThemeDialog = true },
                onOpenStore = {
                    showSettingsMenu = false
                    onOpenStore()
                },
                onPrivacy = {
                    showSettingsMenu = false
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.privacy_policy_url)))
                    )
                }
            )
        }

        if (showThemeDialog) {
            com.turkce.kelimesolitaire.presentation.ui.components.ThemeDialog(
                isPersian = isPersian,
                onDismiss = { showThemeDialog = false },
                onThemeSelected = {
                    showThemeDialog = false
                    showSettingsMenu = false
                }
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

        val outerShape = RoundedCornerShape(24.dp)
        val innerShape = RoundedCornerShape(19.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, outerShape)
                .clip(outerShape)
                .background(
                    if (hasUnclaimedReward) Color(0xFFFFB800).copy(alpha = 0.6f + glowAlpha * 0.4f)
                    else DarkMain
                )
                .candyGloss(corner = 24.dp, inset = 5.dp)
                .padding(8.dp)
                .clip(innerShape)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFFF6E6), Color(0xFFEADFC8)))
                )
                //.glassReflection()
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.12f)
                        )
                    ),
                    innerShape
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
                                modifier = Modifier.size(46.dp),
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
                                    .size(20.dp)
                                    .then(if (isReady) Modifier.scale(pulseScale) else Modifier)
                                    .clip(CircleShape)
                                    .background(
                                        if (isReady)
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFFE066),
                                                    Color(0xFFF59E0B)
                                                )
                                            )
                                        else
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFFF6E6),
                                                    Color(0xFFEADFC8)
                                                )
                                            )
                                    )
                                    .border(
                                        width = if (isReady) 1.5.dp else 2.dp,
                                        color = if (isReady) Color.White else DarkMain,
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
                                        color = DarkMain,
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
                        val isLineActive =
                            day < claimedThisWeek || (day == claimedThisWeek && todayReadyDay == day + 1)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (isLineActive)
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF16A34A), Color(0xFFFFD700))
                                        )
                                    else
                                        Brush.horizontalGradient(
                                            listOf(DarkMain, DarkMain.copy(0.8f))
                                        )
                                )
                        )
                    }
                }
            }
        }
    }
}


