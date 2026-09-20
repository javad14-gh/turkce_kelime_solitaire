package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardClaimStatus
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardItemStatus
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardState
import com.turkce.kelimesolitaire.data.dailyreward.RewardChestType
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.util.rememberAppFont

@Composable
fun DailyRewardDialog(
    state: DailyRewardState,
    onDismiss: () -> Unit,
    onClaim: (doubleReward: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val appFont = rememberAppFont()

    val currentRewardTier = remember(state) {
        state.items.find { it.tier.dayNumber == state.currentClaimDay }?.tier
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .clickable(enabled = false) {}
                    .shadow(24.dp, RoundedCornerShape(26.dp))
                    .border(2.dp, BorderGlass, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocaleHelper.dailyRewardTitle(isPersian),
                                color = AccentGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = appFont
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LocaleHelper.dailyRewardSubtitle(state.cycle, state.currentClaimDay, isPersian),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = appFont
                            )
                        }

                        // Close (X) button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 30-Day Grid (5 columns x 6 rows)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 420.dp),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.items, key = { it.tier.dayNumber }) { item ->
                            DailyRewardCard(
                                item = item,
                                isPersian = isPersian,
                                onClick = {
                                    if (item.status == DailyRewardClaimStatus.READY_TO_CLAIM) {
                                        onClaim(false)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Footer
                    if (state.isReadyToClaimToday && currentRewardTier != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Multiplier Button (2x with Ad)
                            Button(
                                onClick = { onClaim(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(8.dp, RoundedCornerShape(14.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            ),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = LocaleHelper.claim2x(isPersian),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = appFont
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(+${LocaleHelper.formatNumber(currentRewardTier.coins * 2, isPersian)} 🪙)",
                                            color = AccentGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = appFont
                                        )
                                    }
                                }
                            }

                            // Regular Claim (1x)
                            Button(
                                onClick = { onClaim(false) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF10B981), Color(0xFF059669))
                                            ),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = LocaleHelper.claim(isPersian),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = appFont
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(+${LocaleHelper.formatNumber(currentRewardTier.coins, isPersian)} 🪙)",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = appFont
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Already claimed today
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ ${LocaleHelper.comeBackTomorrow(isPersian)}",
                                color = TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = appFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyRewardCard(
    item: DailyRewardItemStatus,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    val tier = item.tier
    val isReady = item.status == DailyRewardClaimStatus.READY_TO_CLAIM
    val isClaimed = item.status == DailyRewardClaimStatus.CLAIMED
    val isMilestone = tier.chestType != RewardChestType.NONE
    val appFont = rememberAppFont()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (isReady) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardScale"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val cardBg = when {
        isReady -> Brush.verticalGradient(
            listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
        )
        isClaimed -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B).copy(alpha = 0.4f), Color(0xFF0F172A).copy(alpha = 0.4f))
        )
        isMilestone -> Brush.verticalGradient(
            listOf(Color(0xFF2A2342), Color(0xFF1E1730))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    }

    val borderColor = when {
        isReady -> AccentGold
        isClaimed -> Color(0xFF10B981).copy(alpha = 0.5f)
        isMilestone -> Color(0xFFEC4899).copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = Modifier
            .scale(pulseScale)
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(
                width = if (isReady) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isReady, onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day Label
            Text(
                text = LocaleHelper.dayLabel(tier.dayNumber, isPersian),
                color = if (isReady) AccentGold else if (isClaimed) Color.Gray else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = appFont
            )

            // Reward Icon / Chest
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isMilestone) {
                    when (tier.chestType) {
                        RewardChestType.BRONZE -> Text("🥉", fontSize = 20.sp)
                        RewardChestType.SILVER -> Text("🥈", fontSize = 20.sp)
                        RewardChestType.GOLD -> Text("🥇", fontSize = 22.sp)
                        RewardChestType.RUBY -> Text("💎", fontSize = 22.sp)
                        RewardChestType.DIAMOND -> Text("👑", fontSize = 24.sp)
                        else -> CoinIcon(size = 20.dp)
                    }
                } else {
                    CoinIcon(size = 20.dp)
                }

                // Claimed Checkmark Overlay
                if (isClaimed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF10B981),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Coin Amount
            Text(
                text = LocaleHelper.formatNumber(tier.coins, isPersian),
                color = if (isClaimed) Color.Gray else if (isReady) AccentGold else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = appFont
            )
        }
    }
}
