package com.turkce.kelimesolitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.turkce.kelimesolitaire.presentation.ui.screens.GameOverScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.GameScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelCompleteScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.MainMenuScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelSelectScreen
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TurkceKelimeSolitaireTheme
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import com.turkce.kelimesolitaire.presentation.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TurkceKelimeSolitaireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GameViewModel = viewModel()
                    val state by viewModel.uiState.collectAsState()

                    // Initialize levels database, progression, and ads
                    LaunchedEffect(Unit) {
                        viewModel.initDatabase(applicationContext)
                        viewModel.initPreferences(applicationContext)
                    }

                    when (state.screenState) {
                        is ScreenState.Loading -> {
                            LoadingView()
                        }
                        is ScreenState.MainMenu -> {
                            MainMenuScreen(
                                levelNumber = state.levelNumber,
                                coins = state.coins,
                                onPlayClicked = { viewModel.navigateToLevelSelect() },
                                onWatchAdForCoins = { viewModel.watchRewardedAdForCoins(this@MainActivity) }
                            )
                        }
                        is ScreenState.LevelSelect -> {
                            LevelSelectScreen(
                                coins = state.coins,
                                completedLevelsStars = state.completedLevelsStars,
                                showResumeDialogForLevel = state.showResumeDialogForLevel,
                                onLevelSelected = { level, activity ->
                                    viewModel.selectLevel(level, activity)
                                },
                                onResumeSelected = { viewModel.resumeActiveGame() },
                                onStartFreshSelected = { viewModel.discardAndStartFresh(this@MainActivity) },
                                onDismissResumeDialog = { viewModel.dismissResumeDialog() },
                                onBackClicked = { viewModel.returnToMainMenu() }
                            )
                        }
                        is ScreenState.Gameplay -> {
                            state.levelData?.let { level ->
                                GameScreen(
                                    levelData = level,
                                    foundationSlots = state.foundationSlots,
                                    tableauPiles = state.tableauPiles,
                                    stockPile = state.stockPile,
                                    wastePile = state.wastePile,
                                    totalWordsToMatch = state.totalWordsToMatch,
                                    selectedCardId = state.selectedCardId,
                                    shakingCardId = state.shakingCardId,
                                    score = state.score,
                                    coins = state.coins,
                                    movesRemaining = state.movesRemaining,
                                    errors = state.errorsInLevel,
                                    onCardSelected = { cardId -> viewModel.selectCard(cardId) },
                                    onCardDropped = { cards, slot ->
                                        viewModel.attemptPlaceCards(cards, slot, this@MainActivity)
                                    },
                                    onCardStacked = { cards, colIdx ->
                                        viewModel.attemptStackCards(cards, colIdx)
                                    },
                                    onDrawFromStock = { viewModel.drawFromStock() },
                                    onRestartLevel = { viewModel.restartLevel(this@MainActivity) },
                                    onBackToMenu = { viewModel.returnToMainMenu() }
                                )
                            } ?: LoadingView()
                        }
                        is ScreenState.LevelComplete -> {
                            LevelCompleteScreen(
                                levelNumber = state.levelNumber,
                                stars = state.starsEarned,
                                bonusCoins = state.levelCompletedBonus,
                                onNextLevelClicked = { viewModel.advanceToNextLevel(this@MainActivity) },
                                onMainMenuClicked = { viewModel.returnToMainMenu() }
                            )
                        }
                        is ScreenState.GameOver -> {
                            GameOverScreen(
                                levelNumber = state.levelNumber,
                                coins = state.coins,
                                onContinueForCoins = { viewModel.purchaseExtraMoves(this@MainActivity) },
                                onContinueForAd = { viewModel.watchAdForExtraMoves(this@MainActivity) },
                                onRestartClicked = { viewModel.restartLevel(this@MainActivity) },
                                onMainMenuClicked = { viewModel.returnToMainMenu() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = SecondaryNeon
        )
    }
}
