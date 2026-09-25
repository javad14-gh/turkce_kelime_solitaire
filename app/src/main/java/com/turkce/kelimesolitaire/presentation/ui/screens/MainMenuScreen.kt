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
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import com.turkce.kelimesolitaire.presentation.ui.components.StarterPackDialog
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
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

    var showSettingsMenu by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isSoundEnabled by remember { androidx.compose.runtime.mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isSoundEnabled(context)) }
    var isHapticEnabled by remember { androidx.compose.runtime.mutableStateOf(com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.isHapticEnabled(context)) }

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
            
            // Top Dashboard HUD (Coins Status Pill left-aligned + Actions right-aligned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A364F).copy(alpha = 0.9f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .clickable { onOpenStore() }
                        .padding(start = 4.dp, end = 16.dp, top = 3.dp, bottom = 3.dp)
                ) {
                    // Gold Coin Icon + Green '+' Circle Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 38.dp)
                        Box(
                            modifier = Modifier
                                .offset(x = 3.dp, y = 3.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LocaleHelper.formatNumber(coins, isPersian),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = nunitoFont
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Daily Reward Gift Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onOpenDailyReward() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gift),
                            contentDescription = "Daily Reward",
                            modifier = Modifier.fillMaxSize()
                        )
                        if (hasUnclaimedDailyReward) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3366))
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    // Settings Icon
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.turkce.kelimesolitaire.R.drawable.setting),
                        contentDescription = "Settings",
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { showSettingsMenu = true }
                    )
                }
            }

            // Central Game Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedText(
                    text = if (isPersian) "پاسور" else "TÜRKÇE KELİME",
                    textColor = SecondaryNeon,
                    outlineColor = Color(0xFF0F172A),
                    outlineWidth = 6f,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
                OutlinedText(
                    text = if (isPersian) "کلمات" else "SOLİTAİRE",
                    textColor = PrimaryNeon,
                    outlineColor = Color(0xFF0F172A),
                    outlineWidth = 6f,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isPersian) "بازی کلمات و سولیتیر" else "Kartları sürükle, kategorileri eşleştir ve seviyeleri tamamla!",
                    color = TextSecondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // 3D Juicy Play Button with Difficulty Ribbon Banner & Starter Pack Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Special Offer: Starter Pack Banner (only shown if not yet purchased)
                if (!isStarterPackPurchased) {
                    StarterPackBanner(
                        isPersian = isPersian,
                        onClick = { showStarterPackDialog = true }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(top = if (isStarterPackPurchased) 14.dp else 4.dp, bottom = 8.dp)
                ) {
                    // Compact 3D Green Button Shell (10% wider than text with 3D bottom bevel)
                    Box(
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                                )
                            ) // Cream/white 3D outer rim shell
                            .padding(4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E3A07)) // Dark 3D bottom base shadow
                            .padding(bottom = 5.dp) // Creates thick 3D bottom bevel
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFA3E635), // Glossy top highlight green
                                        Color(0xFF65A30D), // Mid vibrant green
                                        Color(0xFF4D7C0F)  // Inner shade
                                    )
                                )
                            )
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

        // STARTER PACK SPECIAL OFFER MODAL DIALOG
        if (showStarterPackDialog && !isStarterPackPurchased) {
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
fun StarterPackBanner(
    isPersian: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nunitoFont = rememberNunitoFont()
    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFF3B0764))
                )
            )
            .border(2.dp, AccentGold, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.TopStart) {
                    Image(
                        painter = painterResource(id = R.drawable.gift),
                        contentDescription = "Starter Pack",
                        modifier = Modifier.size(46.dp)
                    )
                    Box(
                        modifier = Modifier
                            .scale(badgeScale)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDC2626))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isPersian) "٪۷۰" else "-70%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPersian) "بسته شروع شگفت‌انگیز" else "BAŞLANGIÇ PAKETİ",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = nunitoFont
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✨",
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPersian) "۲۰۰۰ سکه + ۹ کارت کمکی + بدون تبلیغ" else "2000 Altın + 9 Joker + Reklamsız",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = nunitoFont
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Shiny 3D Gold Price Button
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
                        )
                    )
                    .padding(1.5.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF78350F))
                    .padding(bottom = 2.5.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPersian) "۹۹,۰۰۰ تومان" else "₺99.99",
                    color = Color(0xFF451A03),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = nunitoFont
                )
            }
        }
    }
}

