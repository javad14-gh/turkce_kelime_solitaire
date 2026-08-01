package com.turkce.kelimesolitaire.presentation.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkce.kelimesolitaire.ads.AdManager
import com.turkce.kelimesolitaire.data.model.Category
import com.turkce.kelimesolitaire.data.model.FoundationSlot
import com.turkce.kelimesolitaire.data.model.LevelData
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.data.model.Word
import com.turkce.kelimesolitaire.data.model.WordDatabase
import com.turkce.kelimesolitaire.data.repository.WordRepository
import com.turkce.kelimesolitaire.domain.LevelGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScreenState {
    object MainMenu : ScreenState
    object LevelSelect : ScreenState
    object Loading : ScreenState
    object Gameplay : ScreenState
    object LevelComplete : ScreenState
    object GameOver : ScreenState // Defeat screen
}

data class GameUiState(
    val screenState: ScreenState = ScreenState.MainMenu,
    val levelNumber: Int = 1,
    val score: Int = 0,
    val coins: Int = 100,
    val levelData: LevelData? = null,
    val completedLevelsStars: Map<Int, Int> = emptyMap(),
    
    // Solitaire board state
    val foundationSlots: List<FoundationSlot> = listOf(
        FoundationSlot(0), FoundationSlot(1), FoundationSlot(2), FoundationSlot(3)
    ),
    val tableauPiles: List<List<SolitaireCard>> = listOf(emptyList(), emptyList(), emptyList(), emptyList()),
    val stockPile: List<SolitaireCard> = emptyList(),
    val wastePile: List<SolitaireCard> = emptyList(),
    
    val totalWordsToMatch: Int = 0,
    val totalMatchedWordsCount: Int = 0, // Independent matched progress tracking
    val movesRemaining: Int = 60,
    val selectedCardId: String? = null,
    val shakingCardId: String? = null,
    val starsEarned: Int = 3,
    val levelCompletedBonus: Int = 50,
    val errorsInLevel: Int = 0,
    val showResumeDialogForLevel: Int? = null // Null if no dialog is displayed
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val wordRepository = WordRepository()
    private val levelGenerator = LevelGenerator()
    private var wordDatabase: WordDatabase? = null
    private val adManager = AdManager.getInstance()

    fun initDatabase(context: Context) {
        viewModelScope.launch {
            if (wordDatabase == null) {
                _uiState.update { it.copy(screenState = ScreenState.Loading) }
                val db = wordRepository.loadDatabase(context)
                wordDatabase = db
                adManager.initialize(context) {
                    adManager.loadInterstitial(context)
                    adManager.loadRewarded(context)
                }
                _uiState.update { it.copy(screenState = ScreenState.MainMenu) }
            }
        }
    }

    fun startNewGame(activity: Activity) {
        val db = wordDatabase ?: return
        val currentLvl = _uiState.value.levelNumber
        
        if (currentLvl > 1 && currentLvl % 2 == 0) {
            adManager.showInterstitial(activity) {
                loadLevelData(currentLvl, db)
            }
        } else {
            loadLevelData(currentLvl, db)
        }
    }

    private fun loadLevelData(levelNum: Int, db: WordDatabase) {
        val lastCategoryIds = if (levelNum != _uiState.value.levelNumber) {
            _uiState.value.levelData?.targetCategories?.map { it.id }?.toSet() ?: emptySet()
        } else {
            emptySet()
        }
        val generated = levelGenerator.generateLevel(db, levelNum, lastCategoryIds)
        val allWords = generated.targetWords

        // Initialize 4 empty Foundation slots
        val initialSlots = listOf(
            FoundationSlot(0),
            FoundationSlot(1),
            FoundationSlot(2),
            FoundationSlot(3)
        )

        _uiState.update {
            it.copy(
                screenState = ScreenState.Gameplay,
                levelData = generated,
                foundationSlots = initialSlots,
                tableauPiles = generated.initialTableau,
                stockPile = generated.initialStock,
                wastePile = emptyList(),
                totalWordsToMatch = allWords.size,
                totalMatchedWordsCount = 0,
                movesRemaining = (allWords.size * 3) + 15, // Dynamic moves balance based on board size
                selectedCardId = null,
                shakingCardId = null,
                errorsInLevel = 0
            )
        }
    }

    fun selectCard(cardId: String?) {
        _uiState.update { it.copy(selectedCardId = cardId) }
    }

    fun drawFromStock() {
        val currentStock = _uiState.value.stockPile.toMutableList()
        val currentWaste = _uiState.value.wastePile.toMutableList()

        if (currentStock.isNotEmpty()) {
            val drawn = currentStock.removeAt(0).copy(isFaceUp = true)
            currentWaste.add(drawn)
            _uiState.update {
                it.copy(
                    stockPile = currentStock,
                    wastePile = currentWaste,
                    movesRemaining = maxOf(0, it.movesRemaining - 1),
                    selectedCardId = null
                )
            }
        } else {
            if (currentWaste.isNotEmpty()) {
                val recycled = currentWaste.map { it.copy(isFaceUp = false) }
                _uiState.update {
                    it.copy(
                        stockPile = recycled,
                        wastePile = emptyList(),
                        movesRemaining = maxOf(0, it.movesRemaining - 1),
                        selectedCardId = null
                    )
                }
            }
        }
        checkMovesRemaining()
    }

    fun attemptPlaceCards(cards: List<SolitaireCard>, targetSlot: FoundationSlot, context: Context): Boolean {
        android.util.Log.d("SolitaireDebug", "attemptPlaceCards: cards=${cards.map { it.text }}, targetSlotId=${targetSlot.id}, targetActiveCategory=${targetSlot.activeCategory?.name}")
        if (cards.isEmpty()) return false

        val updatedSlots = _uiState.value.foundationSlots.map { it.copy() }.toMutableList()
        val slotIdx = updatedSlots.indexOfFirst { it.id == targetSlot.id }
        if (slotIdx == -1) return false
        
        val activeSlot = updatedSlots[slotIdx]

        var tempActiveCategory = activeSlot.activeCategory
        val tempMatchedWords = activeSlot.matchedWords.toMutableList()
        
        var successCount = 0
        var scoreDelta = 0
        var coinsDelta = 0
        var newlyMatchedWordsCount = 0

        for (card in cards) {
            if (card.isCategory) {
                // Placing Category Card to activate slot
                if (tempActiveCategory == null) {
                    tempActiveCategory = card.category
                    successCount++
                    scoreDelta += 10
                } else {
                    break // Slot already active, cannot drop category card
                }
            } else {
                // Placing Word Card
                if (tempActiveCategory != null && card.categoryId == tempActiveCategory.id) {
                    val wordToAdd = card.word
                    if (wordToAdd != null) {
                        if (!tempMatchedWords.any { it.id == wordToAdd.id }) {
                            tempMatchedWords.add(wordToAdd)
                            newlyMatchedWordsCount++
                        }
                        successCount++
                        scoreDelta += 10
                        coinsDelta += 2
                    }
                } else {
                    break // Category mismatch
                }
            }
        }

        android.util.Log.d("SolitaireDebug", "  successCount=$successCount, cards.size=${cards.size}")
        if (successCount == cards.size) {
            android.util.Log.d("SolitaireDebug", "  Placement SUCCESS!")
            // Entire stack successfully matched!
            val newSlot = activeSlot.copy(activeCategory = tempActiveCategory, matchedWords = tempMatchedWords)
            updatedSlots[slotIdx] = newSlot

            val (newTableaus, newWaste) = removeCardsFromSource(cards)
            val newTotalMatched = _uiState.value.totalMatchedWordsCount + newlyMatchedWordsCount

            _uiState.update {
                it.copy(
                    foundationSlots = updatedSlots,
                    tableauPiles = newTableaus,
                    wastePile = newWaste,
                    score = it.score + scoreDelta,
                    coins = it.coins + coinsDelta,
                    totalMatchedWordsCount = newTotalMatched,
                    movesRemaining = maxOf(0, it.movesRemaining - 1),
                    selectedCardId = null
                )
            }
            saveCoinsToPrefs(context, _uiState.value.coins)

            // Check if the Category is completed -> Trigger Auto-Clearance Delay (1 second)
            val targetCategory = tempActiveCategory
            if (targetCategory != null) {
                val totalForCategory = _uiState.value.levelData?.targetWords?.count { it.categoryId == targetCategory.id } ?: 0
                if (tempMatchedWords.size >= totalForCategory && totalForCategory > 0) {
                    viewModelScope.launch {
                        delay(1000) // Visual crown exit animation pause
                        val currentSlots = _uiState.value.foundationSlots.toMutableList()
                        val sIdx = currentSlots.indexOfFirst { it.id == targetSlot.id }
                        // Clear slot if same category resides
                        if (sIdx != -1 && currentSlots[sIdx].activeCategory?.id == targetCategory.id) {
                            currentSlots[sIdx] = currentSlots[sIdx].copy(activeCategory = null, matchedWords = emptyList())
                            _uiState.update {
                                it.copy(foundationSlots = currentSlots)
                            }
                        }
                    }
                }
            }

            // Check Victory Condition
            if (newTotalMatched >= _uiState.value.totalWordsToMatch && _uiState.value.totalWordsToMatch > 0) {
                triggerLevelComplete(context)
            } else {
                checkMovesRemaining()
            }
            return true
        } else {
            // Stack contains mismatched cards -> Shake first card grab
            triggerShakeError(cards.first().id)
            return false
        }
    }

    fun attemptStackCards(cards: List<SolitaireCard>, targetColIdx: Int): Boolean {
        if (cards.isEmpty()) return false
        val targetBottom = _uiState.value.tableauPiles[targetColIdx].lastOrNull()

        // Valid stack check: target is empty OR matches category ID of the dragged stack's top card
        if (targetBottom == null || (targetBottom.isFaceUp && targetBottom.categoryId == cards.first().categoryId)) {
            val (newTableaus, newWaste) = removeCardsFromSource(cards)
            val colList = newTableaus[targetColIdx].toMutableList()
            
            // Move entire stack
            colList.addAll(cards.map { it.copy(isFaceUp = true) })
            
            val finalTableaus = newTableaus.mapIndexed { idx, col -> 
                if (idx == targetColIdx) colList else col 
            }

            _uiState.update {
                it.copy(
                    tableauPiles = finalTableaus,
                    wastePile = newWaste,
                    movesRemaining = maxOf(0, it.movesRemaining - 1),
                    selectedCardId = null
                )
            }
            checkMovesRemaining()
            return true
        } else {
            triggerShakeError(cards.first().id)
            return false
        }
    }

    private fun removeCardsFromSource(cards: List<SolitaireCard>): Pair<List<List<SolitaireCard>>, List<SolitaireCard>> {
        val updatedTableaus = _uiState.value.tableauPiles.map { col -> col.toMutableList() }
        val updatedWaste = _uiState.value.wastePile.toMutableList()

        val firstCard = cards.firstOrNull() ?: return Pair(updatedTableaus, updatedWaste)
        var foundInTableau = -1
        for (i in 0..3) {
            if (updatedTableaus[i].any { it.id == firstCard.id }) {
                foundInTableau = i
                break
            }
        }

        if (foundInTableau != -1) {
            val cardIdsToRemove = cards.map { it.id }.toSet()
            updatedTableaus[foundInTableau].removeAll { it.id in cardIdsToRemove }
            val colList = updatedTableaus[foundInTableau]
            if (colList.isNotEmpty()) {
                val lastIdx = colList.size - 1
                colList[lastIdx] = colList[lastIdx].copy(isFaceUp = true) // Auto-flip uncovered card!
            }
        } else if (updatedWaste.lastOrNull()?.id == firstCard.id) {
            updatedWaste.removeAt(updatedWaste.size - 1)
        }

        return Pair(updatedTableaus, updatedWaste)
    }

    private fun triggerShakeError(cardId: String) {
        _uiState.update {
            it.copy(
                shakingCardId = cardId,
                errorsInLevel = it.errorsInLevel + 1,
                movesRemaining = maxOf(0, it.movesRemaining - 1),
                selectedCardId = null
            )
        }
        viewModelScope.launch {
            delay(600)
            if (_uiState.value.shakingCardId == cardId) {
                _uiState.update { it.copy(shakingCardId = null) }
            }
        }
        checkMovesRemaining()
    }

    private fun checkMovesRemaining() {
        if (_uiState.value.movesRemaining <= 0) {
            _uiState.update { it.copy(screenState = ScreenState.GameOver) }
        }
    }

    private fun triggerLevelComplete(context: Context) {
        val currentLvl = _uiState.value.levelNumber
        val initialMoves = (_uiState.value.totalWordsToMatch * 3) + 15
        val remainingMoves = _uiState.value.movesRemaining
        val remainingPercent = (remainingMoves.toFloat() / initialMoves.toFloat()) * 100f

        val stars = when {
            remainingPercent >= 40f -> 3
            remainingPercent >= 15f -> 2
            else -> 1
        }
        
        val baseBonus = when (_uiState.value.levelData?.difficulty) {
            "Kolay" -> 40
            "Orta" -> 60
            "Zor" -> 85
            "CokZor" -> 110
            else -> 40
        }
        val bonus = baseBonus + (stars * 15)

        // Save progress using SharedPreferences
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        val currentStars = prefs.getInt("level_${currentLvl}_stars", 0)
        if (stars > currentStars) {
            prefs.edit().putInt("level_${currentLvl}_stars", stars).apply()
        }

        // Reload the progression map in state
        val updatedMap = _uiState.value.completedLevelsStars.toMutableMap()
        if (stars > (updatedMap[currentLvl] ?: 0)) {
            updatedMap[currentLvl] = stars
        }

        _uiState.update {
            it.copy(
                screenState = ScreenState.LevelComplete,
                starsEarned = stars,
                levelCompletedBonus = bonus,
                coins = it.coins + bonus,
                completedLevelsStars = updatedMap
            )
        }
        saveCoinsToPrefs(context, _uiState.value.coins)
    }

    fun purchaseExtraMoves(activity: Activity) {
        if (_uiState.value.coins >= 50) {
            _uiState.update {
                it.copy(
                    coins = it.coins - 50,
                    movesRemaining = 15,
                    screenState = ScreenState.Gameplay
                )
            }
            saveCoinsToPrefs(activity, _uiState.value.coins)
        } else {
            watchAdForExtraMoves(activity)
        }
    }

    fun watchAdForExtraMoves(activity: Activity) {
        adManager.showRewarded(activity) {
            _uiState.update {
                it.copy(
                    movesRemaining = 15,
                    screenState = ScreenState.Gameplay
                )
            }
        }
    }

    fun restartLevel(activity: Activity) {
        val db = wordDatabase ?: return
        loadLevelData(_uiState.value.levelNumber, db)
    }

    fun advanceToNextLevel(activity: Activity) {
        _uiState.update {
            it.copy(
                levelNumber = it.levelNumber + 1
            )
        }
        startNewGame(activity)
    }

    fun returnToMainMenu() {
        _uiState.update { it.copy(screenState = ScreenState.MainMenu) }
    }

    fun watchRewardedAdForCoins(activity: Activity) {
        adManager.showRewarded(activity) { rewardAmount ->
            _uiState.update {
                it.copy(coins = it.coins + rewardAmount)
            }
            saveCoinsToPrefs(activity, _uiState.value.coins)
        }
    }

    fun initPreferences(context: Context) {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        val map = mutableMapOf<Int, Int>()
        
        // Dynamically load stars for all levels (e.g. level 101+) by iterating over all entries
        val allPrefs = prefs.all
        for ((key, value) in allPrefs) {
            if (key.startsWith("level_") && key.endsWith("_stars")) {
                val levelNumStr = key.substring(6, key.length - 6)
                val levelNum = levelNumStr.toIntOrNull()
                val stars = value as? Int
                if (levelNum != null && stars != null) {
                    map[levelNum] = stars
                }
            }
        }
        
        val savedCoins = prefs.getInt("user_coins", 100)
        _uiState.update { it.copy(completedLevelsStars = map, coins = savedCoins) }
    }

    private fun saveCoinsToPrefs(context: Context, newCoins: Int) {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_coins", newCoins).apply()
    }

    fun navigateToLevelSelect() {
        _uiState.update { it.copy(screenState = ScreenState.LevelSelect) }
    }

    fun selectLevel(levelNum: Int, activity: Activity) {
        val currentLvlData = _uiState.value.levelData
        if (currentLvlData != null && currentLvlData.levelNumber == levelNum &&
            _uiState.value.movesRemaining > 0 &&
            _uiState.value.totalMatchedWordsCount < _uiState.value.totalWordsToMatch
        ) {
            // There is an active in-progress game for this level! Show the resume dialog.
            _uiState.update { it.copy(showResumeDialogForLevel = levelNum) }
        } else {
            // Otherwise, start fresh!
            _uiState.update { it.copy(levelNumber = levelNum, showResumeDialogForLevel = null) }
            startNewGame(activity)
        }
    }

    fun resumeActiveGame() {
        _uiState.update {
            it.copy(
                screenState = ScreenState.Gameplay,
                showResumeDialogForLevel = null
            )
        }
    }

    fun discardAndStartFresh(activity: Activity) {
        val levelNum = _uiState.value.showResumeDialogForLevel ?: _uiState.value.levelNumber
        _uiState.update {
            it.copy(
                levelNumber = levelNum,
                showResumeDialogForLevel = null
            )
        }
        startNewGame(activity)
    }

    fun dismissResumeDialog() {
        _uiState.update { it.copy(showResumeDialogForLevel = null) }
    }
}
