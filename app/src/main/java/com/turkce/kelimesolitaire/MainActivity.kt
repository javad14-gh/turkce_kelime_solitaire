package com.turkce.kelimesolitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.turkce.kelimesolitaire.presentation.ui.screens.GameScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelCompleteScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.LevelLoadingScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.MainMenuScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.SplashScreen
import com.turkce.kelimesolitaire.presentation.ui.screens.StoreScreen
import com.turkce.kelimesolitaire.presentation.ui.components.InGameToastBanner
import com.turkce.kelimesolitaire.presentation.ui.components.MessageType
import com.turkce.kelimesolitaire.presentation.ui.components.RestartLevelDialog
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.presentation.ui.theme.mainBg
import com.turkce.kelimesolitaire.presentation.ui.theme.TurkceKelimeSolitaireTheme
import com.turkce.kelimesolitaire.presentation.viewmodel.GameViewModel
import com.turkce.kelimesolitaire.presentation.viewmodel.ScreenState
import android.content.Intent
import com.turkce.kelimesolitaire.data.billing.InAppBillingManager
import com.turkce.kelimesolitaire.data.billing.MyketBillingConfig
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.turkce.kelimesolitaire.presentation.ui.theme.GameTableBg
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.initPreferences(this)
        viewModel.initDatabase(this)
        com.turkce.kelimesolitaire.presentation.ui.theme.GameThemeManager.init(this)
        InAppBillingManager.getInstance().initialize(this) { ownedSkus ->
            if (ownedSkus.contains(MyketBillingConfig.SKU_REMOVE_ADS) || ownedSkus.contains(MyketBillingConfig.SKU_STARTER_PACK)) {
                viewModel.restorePurchases(this)
            }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                TurkceKelimeSolitaireTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = mainBg
                    ) {
                        val state by viewModel.uiState.collectAsState()

                        val barColor = when (state.screenState) {
                            is ScreenState.Gameplay -> GameTableBg
                            is ScreenState.Splash -> Color.Black
                            else -> mainBg
                        }
                        val darkBackground = state.screenState is ScreenState.Splash

                        SideEffect {
                            window.statusBarColor = barColor.toArgb()
                            window.navigationBarColor = barColor.toArgb()
                            val controller = WindowCompat.getInsetsController(window, window.decorView)
                            controller.isAppearanceLightStatusBars = !darkBackground
                            controller.isAppearanceLightNavigationBars = !darkBackground
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(barColor)                       // رنگ پشت نوار‌ها
                                .windowInsetsPadding(WindowInsets.systemBars)
                        ) {
                            Crossfade(
                                targetState = state.screenState,
                                animationSpec = tween(
                                    durationMillis = 420,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "screenCrossfade"
                            ) { currentScreenState ->
                                when (currentScreenState) {
                                    is ScreenState.Splash -> {
                                        SplashScreen(onSplashFinished = { viewModel.onSplashFinished() })
                                    }

                                    is ScreenState.InitialLoading -> {
                                        LevelLoadingScreen(levelNumber = 0)
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
                                            isStarterPackPurchased = state.isStarterPackPurchased,
                                            hasUnclaimedDailyReward = state.dailyRewardHasUnclaimed,
                                            onStartGameClicked = { level ->
                                                viewModel.playLevel(level, this@MainActivity)
                                            },
                                            onWatchAdForCoins = {
                                                viewModel.watchRewardedAdForCoins(
                                                    this@MainActivity
                                                )
                                            },
                                            onOpenStore = { viewModel.toggleStoreDialog(true) },
                                            onOpenDailyReward = {
                                                if (state.dailyRewardHasUnclaimed) {
                                                    viewModel.claimDailyReward(
                                                        this@MainActivity,
                                                        doubleReward = false
                                                    ) { msg ->
                                                        viewModel.showUserMessage(
                                                            msg,
                                                            MessageType.SUCCESS
                                                        )
                                                    }
                                                } else {
                                                    val msg =
                                                        if (LocaleHelper.isPersian(this@MainActivity)) {
                                                            "جایزه امروز قبلاً دریافت شده است. فردا منتظرتان هستیم!"
                                                        } else {
                                                            "Bugünkü ödül zaten alındı. Yarın tekrar gelin!"
                                                        }
                                                    viewModel.showUserMessage(msg, MessageType.INFO)
                                                }
                                            },
                                            onPurchaseSku = { sku ->
                                                viewModel.purchaseProduct(
                                                    this@MainActivity,
                                                    sku
                                                )
                                            }
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
                                                shouldAnimateDeal = state.shouldAnimateDeal,
                                                onDismissTutorial = { viewModel.dismissTutorial(this@MainActivity) },
                                                onDismissCategoryCelebration = { viewModel.dismissCategoryCelebration() },
                                                onOpenStore = { viewModel.toggleStoreDialog(true) },
                                                onCardSelected = { cardId ->
                                                    viewModel.selectCard(
                                                        cardId
                                                    )
                                                },
                                                onWatchAdForMoves = {
                                                    viewModel.watchAdForExtraMoves(
                                                        this@MainActivity
                                                    )
                                                },
                                                onCardDropped = { cards, slot ->
                                                    viewModel.attemptPlaceCards(
                                                        cards,
                                                        slot,
                                                        this@MainActivity
                                                    )
                                                },
                                                onCardStacked = { cards, colIdx ->
                                                    viewModel.attemptStackCards(
                                                        cards,
                                                        colIdx,
                                                        this@MainActivity
                                                    )
                                                },
                                                onDrawFromStock = { viewModel.drawFromStock(this@MainActivity) },
                                                onRestartLevel = {
                                                    viewModel.requestRestartLevel(
                                                        this@MainActivity
                                                    )
                                                },
                                                onBackToMenu = { viewModel.returnToMainMenu(this@MainActivity) },
                                                onShowMessage = { msg, type ->
                                                    viewModel.showUserMessage(
                                                        msg,
                                                        type
                                                    )
                                                },
                                                onShowHint = {
                                                    viewModel.showHint(this@MainActivity) { msg ->
                                                        viewModel.showUserMessage(
                                                            msg,
                                                            MessageType.INFO
                                                        )
                                                    }
                                                },
                                                onUndoLastMove = {
                                                    viewModel.undoLastMove(this@MainActivity) { msg ->
                                                        viewModel.showUserMessage(
                                                            msg,
                                                            MessageType.WARNING
                                                        )
                                                    }
                                                },
                                                onUseJoker = {
                                                    viewModel.useJoker(this@MainActivity) { msg ->
                                                        viewModel.showUserMessage(
                                                            msg,
                                                            MessageType.WARNING
                                                        )
                                                    }
                                                },
                                                onBuyExtraMoves = {
                                                    viewModel.purchaseExtraMoves(this@MainActivity)
                                                },
                                                onClaimThemeReward = { amount ->
                                                    viewModel.claimThemeReward(
                                                        this@MainActivity,
                                                        amount
                                                    )
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
                                            onDoubleRewardClicked = {
                                                viewModel.doubleLevelRewardWithAd(
                                                    this@MainActivity
                                                )
                                            },
                                            onNormalRewardClicked = {
                                                viewModel.returnToMainMenu(
                                                    this@MainActivity
                                                )
                                            },
                                            onMainMenuClicked = { viewModel.returnToMainMenu(this@MainActivity) },
                                            isAdFree = state.isAdFree
                                        )
                                    }

                                    is ScreenState.GameOver -> {

                                    }

                                    is ScreenState.Store -> {
                                        StoreScreen(
                                            coins = state.coins,
                                            isAdFree = state.isAdFree,
                                            isStarterPackPurchased = state.isStarterPackPurchased,
                                            onClose = { viewModel.closeStore() },
                                            onWatchAdForCoins = {
                                                viewModel.watchRewardedAdForCoins(
                                                    this@MainActivity
                                                )
                                            },
                                            onPurchaseSku = { sku ->
                                                viewModel.purchaseProduct(
                                                    this@MainActivity,
                                                    sku
                                                )
                                            },
                                            onRestorePurchases = { viewModel.restorePurchases(this@MainActivity) }
                                        )
                                    }
                                }
                            }


                            if (state.showRestartDialog) {
                                RestartLevelDialog(
                                    coins = state.coins,
                                    cost = 15,
                                    isFirstFree = state.restartCountInLevel == 0,
                                    onFreeRestart = { viewModel.confirmFreeRestart(this@MainActivity) },
                                    onRestartWithCoins = {
                                        viewModel.confirmRestartWithCoins(this@MainActivity) { msg ->
                                            viewModel.showUserMessage(msg, MessageType.WARNING)
                                        }
                                    },
                                    onRestartWithAd = { viewModel.confirmRestartWithAd(this@MainActivity) },
                                    onDismiss = { viewModel.dismissRestartDialog() }
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

