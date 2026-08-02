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
import com.turkce.kelimesolitaire.data.model.SavedGameSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
    val screenState: ScreenState = ScreenState.Loading,
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
    val showResumeDialogForLevel: Int? = null, // Null if no dialog is displayed
    val hintedCardId: String? = null,
    val hintedTargetId: String? = null,
    val showOutofMovesDialog: Boolean = false,
    val minPossibleMoves: Int = 20,
    val completedCategoryName: String? = null,
    val showEntryBanner: Boolean = true
)

class GameViewModel : ViewModel() {
    private val undoStack = mutableListOf<SavedGameSession>()

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
        
        undoStack.clear() // Clear undo history on fresh start
        // Remove any old saved session since we are starting fresh!
        clearActiveSessionFromPrefs(activity, currentLvl)
        
        if (currentLvl > 1 && currentLvl % 2 == 0) {
            adManager.showInterstitial(activity) {
                loadLevelData(currentLvl, db)
                saveActiveSessionToPrefs(activity)
            }
        } else {
            loadLevelData(currentLvl, db)
            saveActiveSessionToPrefs(activity)
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
                minPossibleMoves = allWords.size + generated.initialStock.size,
                movesRemaining = allWords.size + generated.initialStock.size + 18, // Exact base moves (word count + stock count) + 18 buffer moves
                selectedCardId = null,
                shakingCardId = null,
                errorsInLevel = 0,
                showEntryBanner = true
            )
        }
        android.util.Log.d("SolitaireDebug", "loadLevelData complete, showEntryBanner set to true")
    }

    fun selectCard(cardId: String?) {
        _uiState.update { it.copy(selectedCardId = cardId) }
    }

    fun drawFromStock(context: Context) {
        pushToUndoStack() // PUSH UNDO BEFORE DRAW
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
                    selectedCardId = null,
                    showEntryBanner = false
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
                        selectedCardId = null,
                        showEntryBanner = false
                    )
                }
            }
        }
        checkMovesRemaining()
        saveActiveSessionToPrefs(context)
    }

    fun attemptPlaceCards(cards: List<SolitaireCard>, targetSlot: FoundationSlot, context: Context): Boolean {
        android.util.Log.d("SolitaireDebug", "attemptPlaceCards: cards=${cards.map { it.text }}, targetSlotId=${targetSlot.id}, targetActiveCategory=${targetSlot.activeCategory?.name}, showEntryBanner=${_uiState.value.showEntryBanner}")
        if (cards.isEmpty()) return false

        val updatedSlots = _uiState.value.foundationSlots.map { it.copy() }.toMutableList()
        val slotIdx = updatedSlots.indexOfFirst { it.id == targetSlot.id }
        if (slotIdx == -1) return false
        
        val activeSlot = updatedSlots[slotIdx]

        val currentLvl = _uiState.value.levelNumber
        val isReplay = (_uiState.value.completedLevelsStars[currentLvl] ?: 0) > 0

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
                    val resolvedCategory = card.category ?: _uiState.value.levelData?.targetCategories?.find { it.id == card.categoryId }
                    android.util.Log.d("SolitaireDebug", "  Placing Category card: text=${card.text}, categoryId=${card.categoryId}, resolvedCategoryName=${resolvedCategory?.name}")
                    if (resolvedCategory != null) {
                        tempActiveCategory = resolvedCategory
                        successCount++
                        scoreDelta += 10
                    } else {
                        android.util.Log.e("SolitaireDebug", "  FAILED to resolve category for card: ${card.text}")
                        break
                    }
                } else {
                    break // Slot already active, cannot drop category card
                }
            } else {
                // Placing Word Card
                val actualCategory = tempActiveCategory ?: _uiState.value.levelData?.targetCategories?.find { it.id == card.categoryId }
                if (actualCategory != null && (card.categoryId == actualCategory.id || card.categoryId == "joker_wildcard")) {
                    val wordToAdd = card.word ?: _uiState.value.levelData?.targetWords?.find { it.id == card.id.removePrefix("word_") }
                    if (wordToAdd != null) {
                        if (!tempMatchedWords.any { it.id == wordToAdd.id }) {
                            tempMatchedWords.add(wordToAdd)
                            newlyMatchedWordsCount++
                        }
                        successCount++
                        scoreDelta += 10
                        if (!isReplay) {
                            coinsDelta += 2
                        }
                    } else {
                        android.util.Log.e("SolitaireDebug", "  FAILED to resolve word for card: ${card.text}")
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

            pushToUndoStack() // PUSH UNDO BEFORE PLACE SUCCESS
            _uiState.update {
                it.copy(
                    foundationSlots = updatedSlots,
                    tableauPiles = newTableaus,
                    wastePile = newWaste,
                    score = it.score + scoreDelta,
                    coins = it.coins + coinsDelta,
                    totalMatchedWordsCount = newTotalMatched,
                    movesRemaining = maxOf(0, it.movesRemaining - 1),
                    selectedCardId = null,
                    showEntryBanner = false
                )
            }
            saveCoinsToPrefs(context, _uiState.value.coins)
            
            if (newTotalMatched >= _uiState.value.totalWordsToMatch && _uiState.value.totalWordsToMatch > 0) {
                // triggerLevelComplete deletes the session
            } else {
                saveActiveSessionToPrefs(context)
            }

            // Check if the Category is completed -> Trigger Auto-Clearance Delay (1 second)
            val targetCategory = tempActiveCategory
            if (targetCategory != null) {
                val totalForCategory = _uiState.value.levelData?.targetWords?.count { it.categoryId == targetCategory.id } ?: 0
                if (tempMatchedWords.size >= totalForCategory && totalForCategory > 0) {
                    _uiState.update { it.copy(completedCategoryName = targetCategory.name) }
                    viewModelScope.launch {
                        delay(2200) // Display celebration banner
                        _uiState.update {
                            if (it.completedCategoryName == targetCategory.name) {
                                it.copy(completedCategoryName = null)
                            } else {
                                it
                            }
                        }
                    }
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

    fun attemptStackCards(cards: List<SolitaireCard>, targetColIdx: Int, context: Context): Boolean {
        if (cards.isEmpty()) return false
        
        // Prevent same-column drops from auto-flipping the card underneath or losing moves
        val isFromSameCol = _uiState.value.tableauPiles[targetColIdx].any { it.id == cards.first().id }
        if (isFromSameCol) {
            return false // Returns false without shaking so card slides back smoothly
        }

        val targetBottom = _uiState.value.tableauPiles[targetColIdx].lastOrNull()
        val topDraggedCard = cards.first()
        val isJokerDragged = topDraggedCard.categoryId == "joker_wildcard"
        val isTargetBottomJoker = targetBottom?.isFaceUp == true && targetBottom.categoryId == "joker_wildcard"

        // Valid stack check:
        // 1. target is empty
        // 2. OR target bottom is face-up and has matching category
        // 3. OR the dragged card is the Joker wildcard
        // 4. OR the target bottom card is the Joker wildcard
        val isValidStack = targetBottom == null || 
                           isJokerDragged || 
                           isTargetBottomJoker || 
                           (targetBottom.isFaceUp && targetBottom.categoryId == topDraggedCard.categoryId)

        if (isValidStack) {
            pushToUndoStack() // PUSH UNDO BEFORE TABLEAU MOVE SUCCESS
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
                    selectedCardId = null,
                    showEntryBanner = false
                )
            }
            checkMovesRemaining()
            saveActiveSessionToPrefs(context)
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
            _uiState.update { it.copy(showOutofMovesDialog = true) }
        }
    }

    private fun triggerLevelComplete(context: Context) {
        val currentLvl = _uiState.value.levelNumber
        val minPossible = _uiState.value.minPossibleMoves
        val initialMoves = minPossible + 18
        val remainingMoves = _uiState.value.movesRemaining
        val usedMoves = initialMoves - remainingMoves
        val extraMoves = maxOf(0, usedMoves - minPossible)

        val stars = when {
            extraMoves <= 5 -> 3   // 3 stars for efficient play (at most 5 extra moves)
            extraMoves <= 14 -> 2  // 2 stars for moderately efficient play (at most 14 extra moves)
            else -> 1
        }
        
        val baseBonus = when (stars) {
            3 -> 50
            2 -> 30
            else -> 20
        }

        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        val currentStars = prefs.getInt("level_${currentLvl}_stars", 0)

        val isReplay = currentStars > 0

        val bonus = if (isReplay) {
            if (stars > currentStars) {
                val newBonus = when (stars) {
                    3 -> 50
                    2 -> 30
                    else -> 20
                }
                val oldBonus = when (currentStars) {
                    3 -> 50
                    2 -> 30
                    else -> 20
                }
                newBonus - oldBonus
            } else {
                0
            }
        } else {
            when (stars) {
                3 -> 50
                2 -> 30
                else -> 20
            }
        }

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
        clearActiveSessionFromPrefs(context, currentLvl)
    }

    fun purchaseExtraMoves(activity: Activity) {
        if (_uiState.value.coins >= 100) {
            _uiState.update {
                it.copy(
                    coins = it.coins - 100,
                    movesRemaining = 15,
                    screenState = ScreenState.Gameplay
                )
            }
            saveCoinsToPrefs(activity, _uiState.value.coins)
            saveActiveSessionToPrefs(activity)
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
            saveActiveSessionToPrefs(activity)
        }
    }

    fun restartLevel(activity: Activity) {
        val db = wordDatabase ?: return
        loadLevelData(_uiState.value.levelNumber, db)
        saveActiveSessionToPrefs(activity)
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
        val savedSession = getSavedSession(activity, levelNum)
        val hasActiveProgress = savedSession != null

        if (hasActiveProgress) {
            // There is an active in-progress game for this level! Navigate to level select and show the resume dialog.
            _uiState.update { 
                it.copy(
                    levelNumber = levelNum,
                    showResumeDialogForLevel = levelNum,
                    screenState = ScreenState.LevelSelect
                ) 
            }
        } else {
            // Otherwise, start fresh!
            _uiState.update { it.copy(levelNumber = levelNum, showResumeDialogForLevel = null) }
            startNewGame(activity)
        }
    }

    fun resumeActiveGame(context: Context) {
        val levelNum = _uiState.value.levelNumber
        val session = getSavedSession(context, levelNum)
        if (session != null) {
            undoStack.clear() // Clear undo history on resume
            _uiState.update {
                it.copy(
                    levelNumber = session.levelNumber,
                    levelData = session.levelData,
                    foundationSlots = session.foundationSlots,
                    tableauPiles = session.tableauPiles,
                    stockPile = session.stockPile,
                    wastePile = session.wastePile,
                    score = session.score,
                    movesRemaining = session.movesRemaining,
                    totalMatchedWordsCount = session.totalMatchedWordsCount,
                    screenState = ScreenState.Gameplay,
                    showResumeDialogForLevel = null,
                    selectedCardId = null,
                    shakingCardId = null,
                    showEntryBanner = true
                )
            }
        } else {
            startNewGame(context as Activity)
        }
    }

    fun discardAndStartFresh(activity: Activity) {
        val levelNum = _uiState.value.showResumeDialogForLevel ?: _uiState.value.levelNumber
        clearActiveSessionFromPrefs(activity, levelNum)
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

    private fun saveActiveSessionToPrefs(context: Context) {
        val state = _uiState.value
        val levelData = state.levelData ?: return
        val session = SavedGameSession(
            levelNumber = state.levelNumber,
            levelData = levelData,
            foundationSlots = state.foundationSlots,
            tableauPiles = state.tableauPiles,
            stockPile = state.stockPile,
            wastePile = state.wastePile,
            score = state.score,
            movesRemaining = state.movesRemaining,
            totalMatchedWordsCount = state.totalMatchedWordsCount
        )
        try {
            val jsonString = Json.encodeToString(session)
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("active_game_session_${state.levelNumber}", jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearActiveSessionFromPrefs(context: Context, levelNum: Int) {
        try {
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("active_game_session_$levelNum").apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSavedSession(context: Context, levelNum: Int): SavedGameSession? {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("active_game_session_$levelNum", null) ?: return null
        return try {
            Json.decodeFromString<SavedGameSession>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun pushToUndoStack() {
        val state = _uiState.value
        val levelData = state.levelData ?: return
        val session = SavedGameSession(
            levelNumber = state.levelNumber,
            levelData = levelData,
            foundationSlots = state.foundationSlots,
            tableauPiles = state.tableauPiles,
            stockPile = state.stockPile,
            wastePile = state.wastePile,
            score = state.score,
            movesRemaining = state.movesRemaining,
            totalMatchedWordsCount = state.totalMatchedWordsCount
        )
        undoStack.add(session)
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
    }

    fun undoLastMove(context: Context, onShowToast: (String) -> Unit) {
        if (undoStack.isEmpty()) return
        val state = _uiState.value
        if (state.coins < 50) {
            onShowToast("Yetersiz altın! (50 🪙 gerekli)")
            return
        }
        val prevSession = undoStack.removeAt(undoStack.size - 1)
        _uiState.update {
            it.copy(
                levelNumber = prevSession.levelNumber,
                levelData = prevSession.levelData,
                foundationSlots = prevSession.foundationSlots,
                tableauPiles = prevSession.tableauPiles,
                stockPile = prevSession.stockPile,
                wastePile = prevSession.wastePile,
                score = prevSession.score,
                movesRemaining = prevSession.movesRemaining,
                totalMatchedWordsCount = prevSession.totalMatchedWordsCount,
                coins = maxOf(0, it.coins - 50),
                selectedCardId = null,
                shakingCardId = null
            )
        }
        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        onShowToast("Geri alındı! (-50 🪙)")
    }

    fun showHint(context: Context, onShowToast: (String) -> Unit) {
        val state = _uiState.value
        if (state.coins < 50) {
            onShowToast("Yetersiz altın! (50 🪙 gerekli)")
            return
        }

        // We run the hint engine to see if a hint exists first
        val activeSlots = state.foundationSlots.filter { it.activeCategory != null }
        var hintFound = false
        var hintedCard = ""
        var hintedTarget = ""

        // Check waste top
        val topWaste = state.wastePile.lastOrNull()
        if (topWaste != null) {
            if (topWaste.isCategory) {
                val emptySlot = state.foundationSlots.find { it.activeCategory == null }
                if (emptySlot != null) {
                    hintedCard = topWaste.id
                    hintedTarget = "slot_${emptySlot.id}"
                    hintFound = true
                }
            } else {
                val matchedSlot = activeSlots.find { it.activeCategory?.id == topWaste.categoryId }
                if (matchedSlot != null) {
                    hintedCard = topWaste.id
                    hintedTarget = "slot_${matchedSlot.id}"
                    hintFound = true
                }
            }
        }

        // Check columns last cards
        if (!hintFound) {
            for (colIdx in state.tableauPiles.indices) {
                val col = state.tableauPiles[colIdx]
                val lastCard = col.lastOrNull() ?: continue
                if (lastCard.isFaceUp) {
                    if (lastCard.isCategory) {
                        val emptySlot = state.foundationSlots.find { it.activeCategory == null }
                        if (emptySlot != null) {
                            hintedCard = lastCard.id
                            hintedTarget = "slot_${emptySlot.id}"
                            hintFound = true
                            break
                        }
                    } else {
                        val matchedSlot = activeSlots.find { it.activeCategory?.id == lastCard.categoryId }
                        if (matchedSlot != null) {
                            hintedCard = lastCard.id
                            hintedTarget = "slot_${matchedSlot.id}"
                            hintFound = true
                            break
                        }
                    }
                }
            }
        }

        // Check columns stacking
        if (!hintFound) {
            for (srcColIdx in state.tableauPiles.indices) {
                val srcCol = state.tableauPiles[srcColIdx]
                val topFaceUpIdx = srcCol.indexOfFirst { it.isFaceUp }
                if (topFaceUpIdx == -1) continue
                val topCard = srcCol[topFaceUpIdx]
                
                for (dstColIdx in state.tableauPiles.indices) {
                    if (srcColIdx == dstColIdx) continue
                    val dstCol = state.tableauPiles[dstColIdx]
                    val dstBottom = dstCol.lastOrNull()
                    
                    if (dstBottom == null) {
                        if (topFaceUpIdx > 0) { // Only move stack if it uncovers something
                            hintedCard = topCard.id
                            hintedTarget = "col_$dstColIdx"
                            hintFound = true
                            break
                        }
                    } else if (dstBottom.isFaceUp && dstBottom.categoryId == topCard.categoryId) {
                        hintedCard = topCard.id
                        hintedTarget = "col_$dstColIdx"
                        hintFound = true
                        break
                    }
                }
                if (hintFound) break
            }
        }

        // Check stock suggestion
        if (!hintFound && (state.stockPile.isNotEmpty() || state.wastePile.isNotEmpty())) {
            hintedCard = "stock_pile"
            hintedTarget = "waste_pile"
            hintFound = true
        }

        if (hintFound) {
            _uiState.update {
                it.copy(
                    coins = maxOf(0, it.coins - 50)
                )
            }
            saveCoinsToPrefs(context, _uiState.value.coins)
            saveActiveSessionToPrefs(context)
            onShowToast("İpucu gösterildi! (-50 🪙)")
            triggerHint(hintedCard, hintedTarget)
        } else {
            onShowToast("Yapılabilecek hamle kalmadı!")
        }
    }

    private fun triggerHint(cardId: String, targetId: String) {
        _uiState.update {
            it.copy(
                hintedCardId = cardId,
                hintedTargetId = targetId
            )
        }
        viewModelScope.launch {
            delay(2500)
            if (_uiState.value.hintedCardId == cardId) {
                _uiState.update {
                    it.copy(
                        hintedCardId = null,
                        hintedTargetId = null
                    )
                }
            }
        }
    }

    fun useJoker(context: Context, onShowToast: (String) -> Unit) {
        val state = _uiState.value
        if (state.coins < 200) {
            onShowToast("Yetersiz altın! (200 🪙 gerekli)")
            return
        }

        pushToUndoStack() // PUSH UNDO Snapshots

        // Create the connector Joker wildcard card
        val jokerCard = SolitaireCard(
            id = "joker_card_${System.currentTimeMillis()}",
            text = "🃏 JOKER",
            categoryId = "joker_wildcard",
            isCategory = false,
            isFaceUp = true
        )

        // Add the Joker card to the top of the waste pile
        val updatedWaste = state.wastePile + jokerCard

        _uiState.update {
            it.copy(
                wastePile = updatedWaste,
                coins = maxOf(0, it.coins - 200)
            )
        }
        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        onShowToast("Joker kartı ıskartaya eklendi! (-200 🪙)")
    }

    fun buyExtraMoves(context: Context, onShowToast: (String) -> Unit) {
        val state = _uiState.value
        if (state.coins < 75) {
            onShowToast("Yetersiz altın! (75 🪙 gerekli)")
            return
        }
        _uiState.update {
            it.copy(
                movesRemaining = 5,
                coins = maxOf(0, it.coins - 75),
                showOutofMovesDialog = false
            )
        }
        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        onShowToast("5 Ek Hamle alındı! (-75 🪙)")
    }

    fun dismissEntryBanner() {
        android.util.Log.d("SolitaireDebug", "dismissEntryBanner called, setting showEntryBanner to false")
        _uiState.update { it.copy(showEntryBanner = false) }
    }

    fun acceptDefeat() {
        _uiState.update {
            it.copy(
                showOutofMovesDialog = false,
                screenState = ScreenState.GameOver
            )
        }
    }
}
