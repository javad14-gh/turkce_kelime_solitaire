package com.turkce.kelimesolitaire.presentation.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.theme.AccentGold
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.ui.theme.PrimaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TextPrimary
import com.turkce.kelimesolitaire.presentation.ui.theme.TextSecondary

@Composable
fun LevelSelectScreen(
    coins: Int,
    completedLevelsStars: Map<Int, Int>,
    showResumeDialogForLevel: Int?,
    onLevelSelected: (Int, Activity) -> Unit,
    onResumeSelected: () -> Unit,
    onStartFreshSelected: () -> Unit,
    onDismissResumeDialog: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current as Activity

    BackHandler {
        onBackClicked()
    }

    val totalStars = completedLevelsStars.values.sum()

    // Calculate highest unlocked page (30 levels per page)
    val maxUnlockedPage = remember(completedLevelsStars) {
        var p = 1
        while (true) {
            val lastLvlOfPrevPage = (p - 1) * 30
            if (p > 1 && (completedLevelsStars[lastLvlOfPrevPage] ?: 0) < 1) {
                break
            }
            p++
        }
        p - 1
    }

    var currentPage by remember(maxUnlockedPage) { mutableStateOf(maxUnlockedPage) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Text(
                    text = "◀ Ana Menü",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onBackClicked() }
                        .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                // Coins and Stars HUD bubble
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BorderGlass)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⭐ $totalStars",
                            color = PrimaryNeon,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BorderGlass)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🪙 $coins",
                            color = AccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. MAIN TITLE
            Text(
                text = "BÖLÜM SEÇİN",
                color = PrimaryNeon,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Page Selector Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow Button (Previous Page)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(enabled = currentPage > 1) {
                            if (currentPage > 1) currentPage--
                        }
                        .border(
                            width = 1.dp,
                            color = if (currentPage > 1) BorderGlass else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(if (currentPage > 1) Color.White.copy(alpha = 0.05f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        color = if (currentPage > 1) AccentGold else Color.Gray.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Sayfa $currentPage",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right Arrow Button (Next Page)
                val isNextPageUnlocked = remember(currentPage, completedLevelsStars) {
                    val lastLvlOfCurrentPage = currentPage * 30
                    (completedLevelsStars[lastLvlOfCurrentPage] ?: 0) >= 1
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(enabled = isNextPageUnlocked) {
                            currentPage++
                        }
                        .border(
                            width = 1.dp,
                            color = if (isNextPageUnlocked) BorderGlass else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(if (isNextPageUnlocked) Color.White.copy(alpha = 0.05f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = if (isNextPageUnlocked) AccentGold else Color.Gray.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. LEVEL GRID MAP (30 Levels per page in 5 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(30) { index ->
                    val levelNum = (currentPage - 1) * 30 + index + 1
                    val isUnlocked = levelNum == 1 || (completedLevelsStars[levelNum - 1] ?: 0) >= 1
                    val starsEarned = completedLevelsStars[levelNum] ?: 0

                    LevelCard(
                        levelNum = levelNum,
                        isUnlocked = isUnlocked,
                        starsEarned = starsEarned,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(levelNum, context)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. FOOTER AD
            AdBannerPlaceholder()
        }

        // 5. RESUME / NEW GAME DIALOG OVERLAY
        if (showResumeDialogForLevel != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = true, onClick = onDismissResumeDialog),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(300.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .clickable(enabled = false) {} // Prevent click-through
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Yarıda Kalan Oyun",
                            color = AccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${showResumeDialogForLevel}. seviyede yarıda kalan bir oyununuz var. Devam etmek ister misiniz?",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Fresh Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onStartFreshSelected() }
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Yeni Oyun",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Resume Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onResumeSelected() }
                                    .background(PrimaryNeon, shape = RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Devam Et",
                                    color = Color.Black,
                                    fontSize = 12.sp,
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

@Composable
fun LevelCard(
    levelNum: Int,
    isUnlocked: Boolean,
    starsEarned: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) DarkCard else Color.DarkGray.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .size(width = 64.dp, height = 70.dp)
            .clickable(enabled = isUnlocked) { onClick() }
            .border(
                width = 1.dp,
                color = if (isUnlocked) AccentGold.copy(alpha = 0.6f) else BorderGlass,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNum",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Achievement stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        Text(
                            text = "★",
                            color = if (i <= starsEarned) AccentGold else Color.Gray.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "🔒",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bölüm $levelNum",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
