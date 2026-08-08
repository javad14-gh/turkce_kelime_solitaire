package com.turkce.kelimesolitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.turkce.kelimesolitaire.presentation.ui.screens.GameOverScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.GameScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelCompleteScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.MainMenuScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.StoreScreen
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TurkceKelimeSolitaireTheme
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import com.turkce.kelimesolitaire.presentation.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.initPreferences(this)
        viewModel.initDatabase(this)

        setContent {
            TurkceKelimeSolitaireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.uiState.collectAsState()

                    when (state.screenState) {
                        is ScreenState.Loading -> {
                            LoadingView()
                        }
                        is ScreenState.MainMenu -> {
                            MainMenuScreen(
                                levelNumber = state.levelNumber,
                                coins = state.coins,
                                completedLevels = state.completedLevels,
                                isAdFree = state.isAdFree,
                                onStartGameClicked = { level ->
                                    viewModel.playLevel(level, this@MainActivity)
                                },
                                onWatchAdForCoins = { viewModel.watchRewardedAdForCoins(this@MainActivity) },
                                onOpenStore = { viewModel.toggleStoreDialog(true) }
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
                                    completedLevels = state.completedLevels,
                                    movesRemaining = state.movesRemaining,
                                    errors = state.errorsInLevel,
                                    hintedCardId = state.hintedCardId,
                                    hintedTargetId = state.hintedTargetId,
                                    showOutofMovesDialog = state.showOutofMovesDialog,
                                    completedCategoryName = state.completedCategoryName,
                                    shatteringJokerId = state.shatteringJokerId,
                                    isAdFree = state.isAdFree,
                                    onOpenStore = { viewModel.toggleStoreDialog(true) },
                                    onCardSelected = { cardId -> viewModel.selectCard(cardId) },
                                    onCardDropped = { cards, slot ->
                                        viewModel.attemptPlaceCards(cards, slot, this@MainActivity)
                                    },
                                    onCardStacked = { cards, colIdx ->
                                        viewModel.attemptStackCards(cards, colIdx, this@MainActivity)
                                    },
                                    onDrawFromStock = { viewModel.drawFromStock(this@MainActivity) },
                                    onRestartLevel = { viewModel.restartLevel(this@MainActivity) },
                                    onBackToMenu = { viewModel.returnToMainMenu() },
                                    onShowHint = {
                                        viewModel.showHint(this@MainActivity) { msg ->
                                            android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onUndoLastMove = {
                                        viewModel.undoLastMove(this@MainActivity) { msg ->
                                            android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onUseJoker = {
                                        viewModel.useJoker(this@MainActivity) { msg ->
                                            android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onBuyExtraMoves = {
                                        viewModel.buyExtraMoves(this@MainActivity) { msg ->
                                            android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAcceptDefeat = {
                                        viewModel.acceptDefeat()
                                    }
                                )
                            } ?: LoadingView()
                        }
                        is ScreenState.LevelComplete -> {
                            LevelCompleteScreen(
                                levelNumber = state.levelNumber,
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
                        is ScreenState.Store -> {
                            StoreScreen(
                                coins = state.coins,
                                isAdFree = state.isAdFree,
                                onClose = { viewModel.closeStore() },
                                onWatchAdForCoins = { viewModel.watchRewardedAdForCoins(this@MainActivity) },
                                onBuyCoinPack = { amount -> viewModel.buyCoinPack(this@MainActivity, amount) },
                                onBuyRemoveAds = { viewModel.buyRemoveAds(this@MainActivity) }
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
