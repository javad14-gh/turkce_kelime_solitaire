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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.turkce.kelimesolitaire.presentation.ui.screens.GameOverScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.GameScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelCompleteScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelLoadingScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.MainMenuScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.SplashScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.StoreScreen
import android.widget.Toast
import com.turkce.kelimesolitaire.presentation.ui.components.DailyRewardDialog
import com.turkce.kelimesolitaire.presentation.ui.components.InGameToastBanner
import com.turkce.kelimesolitaire.presentation.ui.components.MessageType
import com.turkce.kelimesolitaire.presentation.ui.components.RestartLevelDialog
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.ui.theme.SecondaryNeon
import com.turkce.kelimesolitaire.presentation.ui.theme.TurkceKelimeSolitaireTheme
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import com.turkce.kelimesolitaire.presentation.viewmodel.ScreenState
import android.content.Intent
import com.turkce.kelimesolitaire.data.billing.InAppBillingManager
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.initPreferences(this)
        viewModel.initDatabase(this)
        InAppBillingManager.getInstance().initialize(this) { ownedSkus ->
            if (ownedSkus.contains(MyketBillingConfig.SKU_REMOVE_ADS) || ownedSkus.contains(MyketBillingConfig.SKU_STARTER_PACK)) {
                viewModel.restorePurchases(this)
            }
        }

        setContent {
            TurkceKelimeSolitaireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.uiState.collectAsState()

                    Crossfade(
                        targetState = state.screenState,
                        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                        label = "screenCrossfade"
                    ) { currentScreenState ->
                        when (currentScreenState) {
                            is ScreenState.Splash -> {
                                SplashScreen(
                                    onSplashFinished = { viewModel.onSplashFinished() }
                                )
                            }
                            is ScreenState.Loading -> {
                                LevelLoadingScreen(
                                    levelNumber = state.levelNumber
                                )
                            }
                            is ScreenState.MainMenu -> {
                                MainMenuScreen(
                                    levelNumber = state.levelNumber,
                                    coins = state.coins,
                                    completedLevels = state.completedLevels,
                                    isAdFree = state.isAdFree,
                                    hasUnclaimedDailyReward = state.dailyRewardHasUnclaimed,
                                    onStartGameClicked = { level ->
                                        viewModel.playLevel(level, this@MainActivity)
                                    },
                                    onWatchAdForCoins = { viewModel.watchRewardedAdForCoins(this@MainActivity) },
                                    onOpenStore = { viewModel.toggleStoreDialog(true) },
                                    onOpenDailyReward = { viewModel.openDailyRewardDialog(this@MainActivity) }
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
                                        isLevelWon = state.isLevelWon,
                                        shatteringJokerId = state.shatteringJokerId,
                                        isAdFree = state.isAdFree,
                                        isUndoUnlocked = state.isUndoUnlocked,
                                        isHintUnlocked = state.isHintUnlocked,
                                        isJokerUnlocked = state.isJokerUnlocked,
                                        hasFreeUndo = state.hasFreeUndo,
                                        hasFreeHint = state.hasFreeHint,
                                        hasFreeJoker = state.hasFreeJoker,
                                        freeUndoCount = state.freeUndoCount,
                                        freeHintCount = state.freeHintCount,
                                        freeJokerCount = state.freeJokerCount,
                                        activeTutorial = state.activeTutorial,
                                        level1TutorialStep = state.level1TutorialStep,
                                        shouldAnimateDeal = state.shouldAnimateDeal,
                                        onDismissTutorial = { viewModel.dismissTutorial(this@MainActivity) },
                                        onDismissCategoryCelebration = { viewModel.dismissCategoryCelebration() },
                                        onOpenStore = { viewModel.toggleStoreDialog(true) },
                                        onCardSelected = { cardId -> viewModel.selectCard(cardId) },
                                        onCardDropped = { cards, slot ->
                                            viewModel.attemptPlaceCards(cards, slot, this@MainActivity)
                                        },
                                        onCardStacked = { cards, colIdx ->
                                            viewModel.attemptStackCards(cards, colIdx, this@MainActivity)
                                        },
                                        onDrawFromStock = { viewModel.drawFromStock(this@MainActivity) },
                                        onRestartLevel = { viewModel.requestRestartLevel(this@MainActivity) },
                                        onBackToMenu = { viewModel.returnToMainMenu() },
                                        onShowMessage = { msg, type -> viewModel.showUserMessage(msg, type) },
                                        onShowHint = {
                                            viewModel.showHint(this@MainActivity) { msg ->
                                                viewModel.showUserMessage(msg, MessageType.INFO)
                                            }
                                        },
                                        onUndoLastMove = {
                                            viewModel.undoLastMove(this@MainActivity) { msg ->
                                                viewModel.showUserMessage(msg, MessageType.WARNING)
                                            }
                                        },
                                        onUseJoker = {
                                            viewModel.useJoker(this@MainActivity) { msg ->
                                                viewModel.showUserMessage(msg, MessageType.WARNING)
                                            }
                                        },
                                        onBuyExtraMoves = {
                                            viewModel.buyExtraMoves(this@MainActivity) { msg ->
                                                viewModel.showUserMessage(msg, MessageType.WARNING)
                                            }
                                        },
                                        onAcceptDefeat = {
                                            viewModel.acceptDefeat()
                                        }
                                    )
                                } ?: LevelLoadingScreen(
                                    levelNumber = state.levelNumber
                                )
                            }
                            is ScreenState.LevelComplete -> {
                                LevelCompleteScreen(
                                    levelNumber = state.levelNumber,
                                    bonusCoins = state.levelCompletedBonus,
                                    isRewardDoubled = state.isLevelRewardDoubled,
                                    onDoubleRewardClicked = { viewModel.doubleLevelRewardWithAd(this@MainActivity) },
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
                                    isStarterPackPurchased = state.isStarterPackPurchased,
                                    onClose = { viewModel.closeStore() },
                                    onWatchAdForCoins = { viewModel.watchRewardedAdForCoins(this@MainActivity) },
                                    onPurchaseSku = { sku -> viewModel.purchaseProduct(this@MainActivity, sku) },
                                    onRestorePurchases = { viewModel.restorePurchases(this@MainActivity) }
                                )
                            }
                        }
                    }

                    if (state.screenState is ScreenState.MainMenu && state.showDailyRewardDialog && state.dailyRewardState != null) {
                        DailyRewardDialog(
                            state = state.dailyRewardState!!,
                            onDismiss = { viewModel.dismissDailyRewardDialog() },
                            onClaim = { doubleReward ->
                                viewModel.claimDailyReward(this@MainActivity, doubleReward) { msg ->
                                    viewModel.showUserMessage(msg, MessageType.SUCCESS)
                                }
                            }
                        )
                    }

                    if (state.showRestartDialog) {
                        RestartLevelDialog(
                            coins = state.coins,
                            cost = 15,
                            onRestartWithCoins = {
                                viewModel.confirmRestartWithCoins(this@MainActivity) { msg ->
                                    viewModel.showUserMessage(msg, MessageType.WARNING)
                                }
                            },
                            onRestartWithAd = {
                                viewModel.confirmRestartWithAd(this@MainActivity)
                            },
                            onDismiss = {
                                viewModel.dismissRestartDialog()
                            }
                        )
                    }

                    // Global in-game toast banner with unified fonts & smooth animations
                    InGameToastBanner(
                        message = state.activeMessage,
                        onDismiss = { viewModel.dismissUserMessage() }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        InAppBillingManager.getInstance().handleActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data,
            onSuccess = { sku ->
                viewModel.fulfillPurchase(sku, this)
            },
            onError = { errorMsg ->
                viewModel.showUserMessage(errorMsg, MessageType.ERROR)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        InAppBillingManager.getInstance().unbind(this)
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
