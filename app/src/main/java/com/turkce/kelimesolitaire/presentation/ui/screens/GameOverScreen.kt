package com.turkce.kelimesolitaire.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.OutlinedText
import com.turkce.kelimesolitaire.presentation.ui.components.TvIcon
import com.turkce.kelimesolitaire.presentation.ui.components.candyGloss
import com.turkce.kelimesolitaire.presentation.ui.components.specularGlint
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.mainBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupButtonBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayDepth
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnTop
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnMid
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnBottom
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameGradient
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameBase
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.components.IconLabelRow

@Composable
fun GameOverScreen(
    levelNumber: Int,
    coins: Int,
    onContinueForCoins: () -> Unit,
    onContinueForAd: () -> Unit,
    onRestartClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    isAdFree: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()

    val cost = GameViewModel.EXTRA_MOVES_COIN_COST
    val movesForCoins = GameViewModel.EXTRA_MOVES_FOR_COINS
    val movesForAd = GameViewModel.EXTRA_MOVES_FOR_AD
    val canAfford = coins >= cost

    val coinLabel = if (isPersian) {
        "${LocaleHelper.formatNumber(cost, true)} سکه (\u200F+${LocaleHelper.formatNumber(movesForCoins, true)} حرکت)"
    } else {
        "$cost Altın (+$movesForCoins Hamle)"
    }
    val adLabel = if (isPersian) {
        "ویدیو (\u200F+${LocaleHelper.formatNumber(movesForAd, true)} حرکت)"
    } else {
        "Video İzle (+$movesForAd Hamle)"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mainBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---- Title ----
                OutlinedText(
                    text = LocaleHelper.defeatTitle(isPersian),
                    textColor = Color.White,
                    outlineColor = MenuBtnOutline,
                    outlineWidth = 12.5f,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = LocaleHelper.defeatSubtitle(levelNumber, isPersian),
                    color = MenuBtnMid.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = nunitoFont,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ---- Continue card: dark 3D frame + header + light tray ----
                Box(
                    modifier = Modifier
                        .widthIn(max = 330.dp)
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(PopupFrameBase)
                        .padding(bottom = 7.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(PopupFrameGradient)
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedText(
                                text = if (isPersian) "ادامه بازی" else "Oyuna Devam Et",
                                textColor = Color.White,
                                outlineColor = PopupTitleOutline,
                                outlineWidth = 12.5f,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                                .shadow(10.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(PopupTrayDepth)
                                .padding(bottom = 5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PopupBg)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Coins balance pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PopupButtonBg)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    CoinIcon(size = 20.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${LocaleHelper.formatNumber(coins, isPersian)} " +
                                                if (isPersian) "سکه موجود" else "Mevcut Altın",
                                        color = PopupTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = nunitoFont
                                    )
                                }

                                // 1. Continue with coins (gold)
                                GameOverButton(
                                    modifier = Modifier.fillMaxWidth(0.88f),
                                    baseColor = if (canAfford) Color(0xFF78350F) else Color(0xFF475569),
                                    face = if (canAfford) {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFD97706))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                        )
                                    },
                                    enabled = canAfford,
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onContinueForCoins()
                                    }
                                ) {
                                    IconLabelRow(
                                        isPersian = isPersian,
                                        icon = { CoinIcon(size = 30.dp) },
                                        label = {
                                            val outline = if (canAfford) Color(0xFF78350F) else Color(0xFF475569)
                                            OutlinedText(
                                                text = coinLabel,
                                                textColor = Color.White,
                                                outlineColor = outline,
                                                outlineWidth = 6f,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center,
                                                shadowColor = outline.copy(alpha = 0.6f),
                                                shadowOffsetY = 3f
                                            )
                                        }
                                    )
                                }

                                // 2. Continue with rewarded video (blue)
                                GameOverButton(
                                    modifier = Modifier.fillMaxWidth(0.88f),
                                    baseColor = Color(0xFF0C4A6E),
                                    face = Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                                    ),
                                    onClick = {
                                        GameSettingsManager.playButtonClickSound(context)
                                        onContinueForAd()
                                    }
                                ) {
                                    IconLabelRow(
                                        isPersian = isPersian,
                                        icon = { TvIcon(size = 28.dp) },
                                        label = {
                                            val outline = Color(0xFF0C4A6E)
                                            OutlinedText(
                                                text = adLabel,
                                                textColor = Color.White,
                                                outlineColor = outline,
                                                outlineWidth = 6f,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Black,
                                                textAlign = TextAlign.Center,
                                                shadowColor = outline.copy(alpha = 0.6f),
                                                shadowOffsetY = 3f
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ---- Retry (popup style) ----
                GameOverButton(
                    modifier = Modifier.fillMaxWidth(0.72f),
                    baseColor = MenuBtnOutline,
                    face = Brush.verticalGradient(listOf(MenuBtnTop, MenuBtnMid, MenuBtnBottom)),
                    minHeight = 68.dp,
                    onClick = {
                        GameSettingsManager.playButtonClickSound(context)
                        onRestartClicked()
                    }
                ) {
                    IconLabelRow(
                        isPersian = isPersian,
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.restart),
                                contentDescription = "Retry",
                                modifier = Modifier.size(46.dp)
                            )
                        },
                        label = {
                            OutlinedText(
                                text = LocaleHelper.retry(isPersian),
                                textColor = Color.White,
                                outlineColor = MenuBtnOutline,
                                outlineWidth = 12.5f,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---- Main menu (red) ----
                GameOverButton(
                    modifier = Modifier.fillMaxWidth(0.72f),
                    baseColor = Color(0xFF7F1D1D),
                    face = Brush.verticalGradient(
                        listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFFB91C1C))
                    ),
                    minHeight = 68.dp,
                    onClick = {
                        GameSettingsManager.playButtonClickSound(context)
                        onMainMenuClicked()
                    }
                ) {
                    IconLabelRow(
                        isPersian = isPersian,
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.exit),
                                contentDescription = "Main Menu",
                                modifier = Modifier.size(46.dp)
                            )
                        },
                        label = {
                            OutlinedText(
                                text = LocaleHelper.mainMenu(isPersian),
                                textColor = Color.White,
                                outlineColor = Color(0xFF7F1D1D),
                                outlineWidth = 12.5f,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            AdBannerPlaceholder(isAdFree = isAdFree)
        }
    }
}

/** Shared 3D Candy Button matching the store pack buttons. */
@Composable
private fun GameOverButton(
    baseColor: Color,
    face: Brush,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 60.dp,
    corner: Dp = 16.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .alpha(if (enabled) 1f else 0.7f)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(face)
            .candyGloss(corner = corner, inset = 2.dp)
            .specularGlint(offsetX = 12.dp, offsetY = 5.dp, scale = 1.15f)
            .border(1.dp, baseColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
