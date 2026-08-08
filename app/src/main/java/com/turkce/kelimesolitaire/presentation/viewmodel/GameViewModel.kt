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
    object Loading : ScreenState
    object Gameplay : ScreenState
    object LevelComplete : ScreenState
    object GameOver : ScreenState // Defeat screen
    object Store : ScreenState // Full-screen store
}

data class GameUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val previousScreenState: ScreenState = ScreenState.MainMenu,
    val levelNumber: Int = 1,
    val score: Int = 0,
    val coins: Int = 100,
    val levelData: LevelData? = null,
    val completedLevels: Set<Int> = emptySet(),
    
    // Solitaire board state
    val foundationSlots: List<FoundationSlot> = listOf(
        FoundationSlot(0), FoundationSlot(1), FoundationSlot(2), FoundationSlot(3)
    ),
    val tableauPiles: List<List<SolitaireCard>> = listOf(emptyList(), emptyList(), emptyList(), emptyList()),
    val stockPile: List<SolitaireCard> = emptyList(),
    val wastePile: List<SolitaireCard> = emptyList(),
    
    val totalWordsToMatch: Int = 0,
    val totalMatchedWordsCount: Int = 0,
    val movesRemaining: Int = 60,
    val selectedCardId: String? = null,
    val shakingCardId: String? = null,
    val levelCompletedBonus: Int = 50,
    val errorsInLevel: Int = 0,
    val hintedCardId: String? = null,
    val hintedTargetId: String? = null,
    val showOutofMovesDialog: Boolean = false,
    val completedCategoryName: String? = null,
    val shatteringJokerId: String? = null,
    val isAdFree: Boolean = false,
    val showStoreDialog: Boolean = false
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

        val initialSlots = listOf(
            FoundationSlot(0),
            FoundationSlot(1),
            FoundationSlot(2),
            FoundationSlot(3)
        )

        _uiState.update {
            it.copy(
                screenState = ScreenState.Gameplay,
                levelNumber = levelNum,
                levelData = generated,
                foundationSlots = initialSlots,
                tableauPiles = generated.initialTableau,
                stockPile = generated.initialStock,
                wastePile = emptyList(),
                totalWordsToMatch = allWords.size,
                totalMatchedWordsCount = 0,
                movesRemaining = allWords.size + generated.initialStock.size + 18,
                selectedCardId = null,
                shakingCardId = null,
                errorsInLevel = 0
            )
        }
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
        saveActiveSessionToPrefs(context)
    }

    fun attemptPlaceCards(cards: List<SolitaireCard>, targetSlot: FoundationSlot, context: Context): Boolean {
        if (cards.isEmpty()) return false

        val updatedSlots = _uiState.value.foundationSlots.map { it.copy() }.toMutableList()
        val slotIdx = updatedSlots.indexOfFirst { it.id == targetSlot.id }
        if (slotIdx == -1) return false
        
        val activeSlot = updatedSlots[slotIdx]
        val currentLvl = _uiState.value.levelNumber
        val isReplay = _uiState.value.completedLevels.contains(currentLvl)

        var tempActiveCategory = activeSlot.activeCategory
        val tempMatchedWords = activeSlot.matchedWords.toMutableList()
        
        var successCount = 0
        var scoreDelta = 0
        var coinsDelta = 0
        var newlyMatchedWordsCount = 0

        for (card in cards) {
            if (card.isCategory) {
                if (tempActiveCategory == null) {
                    val resolvedCategory = card.category ?: _uiState.value.levelData?.targetCategories?.find { it.id == card.categoryId }
                    if (resolvedCategory != null) {
                        tempActiveCategory = resolvedCategory
                        successCount++
                        scoreDelta += 10
                    } else {
                        break
                    }
                } else {
                    break
                }
            } else {
                // Word cards can ONLY be placed if a category card is ALREADY active in this slot (or placed first in this batch)
                if (tempActiveCategory == null) {
                    break
                }

                val actualCategory = tempActiveCategory
                if (card.categoryId == actualCategory.id || card.categoryId == "joker_wildcard") {
                    val wordToAdd = if (card.categoryId == "joker_wildcard") {
                        card.word ?: com.turkce.kelimesolitaire.data.model.Word(card.id, actualCategory.id, "JOKER", "Kolay")
                    } else {
                        card.word ?: _uiState.value.levelData?.targetWords?.find { it.id == card.id.removePrefix("word_") }
                    }

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
                    }
                } else {
                    break
                }
            }
        }

        if (successCount == cards.size) {
            val newSlot = activeSlot.copy(activeCategory = tempActiveCategory, matchedWords = tempMatchedWords)
            updatedSlots[slotIdx] = newSlot

            val (newTableaus, newWaste) = removeCardsFromSource(cards)
            val newTotalMatched = _uiState.value.totalMatchedWordsCount + newlyMatchedWordsCount

            pushToUndoStack()
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
            
            if (newTotalMatched < _uiState.value.totalWordsToMatch || _uiState.value.totalWordsToMatch == 0) {
                saveActiveSessionToPrefs(context)
            }
            checkExposedJokers()

            val targetCategory = tempActiveCategory
            if (targetCategory != null) {
                val totalForCategory = _uiState.value.levelData?.targetWords?.count { it.categoryId == targetCategory.id } ?: 4
                val requiredCount = if (totalForCategory > 0) totalForCategory else 4
                if (tempMatchedWords.isNotEmpty() && tempMatchedWords.size >= requiredCount) {
                    _uiState.update { it.copy(completedCategoryName = targetCategory.name) }
                    viewModelScope.launch {
                        delay(2200)
                        _uiState.update {
                            if (it.completedCategoryName == targetCategory.name) {
                                it.copy(completedCategoryName = null)
                            } else {
                                it
                            }
                        }
                    }
                    viewModelScope.launch {
                        delay(1000)
                        val currentSlots = _uiState.value.foundationSlots.toMutableList()
                        val sIdx = currentSlots.indexOfFirst { it.id == targetSlot.id }
                        if (sIdx != -1 && currentSlots[sIdx].activeCategory?.id == targetCategory.id) {
                            currentSlots[sIdx] = currentSlots[sIdx].copy(activeCategory = null, matchedWords = emptyList())
                            _uiState.update {
                                it.copy(foundationSlots = currentSlots)
                            }
                        }
                    }
                }
            }

            if (newTotalMatched >= _uiState.value.totalWordsToMatch && _uiState.value.totalWordsToMatch > 0) {
                triggerLevelComplete(context)
            }
            return true
        } else {
            triggerShakeError(cards.firstOrNull()?.id)
            return false
        }
    }

    fun attemptStackCards(cards: List<SolitaireCard>, targetColIdx: Int, context: Context): Boolean {
        if (cards.isEmpty()) return false
        val tableaus = _uiState.value.tableauPiles.map { it.toMutableList() }
        if (targetColIdx !in 0..3) return false

        val movingTopCard = cards.first()

        // If cards are dropped back onto the exact same column they came from, it's a no-op
        val sourceColIdx = _uiState.value.tableauPiles.indexOfFirst { col -> col.any { it.id == movingTopCard.id } }
        if (sourceColIdx == targetColIdx) {
            return false
        }
        
        val targetCol = tableaus[targetColIdx]
        var isValidStack = false

        if (targetCol.isEmpty()) {
            isValidStack = true
        } else {
            val destinationBottomCard = targetCol.last()
            if (destinationBottomCard.isFaceUp) {
                if (movingTopCard.categoryId == destinationBottomCard.categoryId || 
                    movingTopCard.categoryId == "joker_wildcard" || 
                    destinationBottomCard.categoryId == "joker_wildcard") {
                    isValidStack = true
                }
            }
        }

        if (isValidStack) {
            pushToUndoStack()
            val (newTableaus, newWaste) = removeCardsFromSource(cards)
            val colList = newTableaus[targetColIdx].toMutableList()
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
            saveActiveSessionToPrefs(context)
            checkExposedJokers()
            return true
        } else {
            triggerShakeError(movingTopCard.id)
            return false
        }
    }

    private val coveredJokerIds = mutableSetOf<String>()

    fun checkExposedJokers() {
        val currentState = _uiState.value
        if (currentState.shatteringJokerId != null) return

        // 1. Mark any Joker that has cards stacked on top of it as covered
        for (col in currentState.tableauPiles) {
            for (i in 0 until col.size - 1) {
                if (col[i].categoryId == "joker_wildcard") {
                    coveredJokerIds.add(col[i].id)
                }
            }
        }

        // 2. Find a Joker that WAS covered and is NOW exposed at the top of a column
        var exposedJokerId: String? = null
        for (col in currentState.tableauPiles) {
            if (col.isNotEmpty()) {
                val lastCard = col.last()
                if (lastCard.categoryId == "joker_wildcard" && coveredJokerIds.contains(lastCard.id)) {
                    exposedJokerId = lastCard.id
                    break
                }
            }
        }

        if (exposedJokerId != null) {
            coveredJokerIds.remove(exposedJokerId)
            triggerJokerShatter(exposedJokerId)
        }
    }

    private fun triggerJokerShatter(jokerId: String) {
        _uiState.update { it.copy(shatteringJokerId = jokerId) }
        viewModelScope.launch {
            delay(650)
            consumeAndRemoveJoker(jokerId)
        }
    }

    private fun consumeAndRemoveJoker(jokerId: String) {
        _uiState.update { state ->
            val newWaste = state.wastePile.filterNot { it.id == jokerId }
            val newTableaus = state.tableauPiles.map { col ->
                val filtered = col.filterNot { it.id == jokerId }.toMutableList()
                if (filtered.isNotEmpty()) {
                    val lastIdx = filtered.size - 1
                    filtered[lastIdx] = filtered[lastIdx].copy(isFaceUp = true)
                }
                filtered.toList()
            }
            state.copy(
                wastePile = newWaste,
                tableauPiles = newTableaus,
                shatteringJokerId = null
            )
        }
        checkExposedJokers()
    }

    private fun removeCardsFromSource(cards: List<SolitaireCard>): Pair<List<List<SolitaireCard>>, List<SolitaireCard>> {
        val cardIds = cards.map { it.id }.toSet()
        val newWaste = _uiState.value.wastePile.filterNot { it.id in cardIds }
        
        val newTableaus = _uiState.value.tableauPiles.map { col ->
            val updatedCol = col.filterNot { it.id in cardIds }.toMutableList()
            if (updatedCol.isNotEmpty()) {
                val lastIdx = updatedCol.size - 1
                updatedCol[lastIdx] = updatedCol[lastIdx].copy(isFaceUp = true)
            }
            updatedCol.toList()
        }

        return Pair(newTableaus, newWaste)
    }

    private fun triggerShakeError(cardId: String?) {
        if (cardId == null) return
        _uiState.update {
            it.copy(
                shakingCardId = cardId,
                errorsInLevel = it.errorsInLevel + 1
            )
        }
        viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(shakingCardId = null) }
        }
    }

    private fun checkMovesRemaining() {
        if (_uiState.value.movesRemaining <= 0 && _uiState.value.totalMatchedWordsCount < _uiState.value.totalWordsToMatch) {
            _uiState.update { it.copy(showOutofMovesDialog = true) }
        }
    }

    private fun triggerLevelComplete(context: Context) {
        val currentLvl = _uiState.value.levelNumber
        val isReplay = _uiState.value.completedLevels.contains(currentLvl)
        val bonus = if (isReplay) 0 else 50

        val updatedSet = _uiState.value.completedLevels + currentLvl

        _uiState.update {
            it.copy(
                screenState = ScreenState.LevelComplete,
                levelCompletedBonus = bonus,
                coins = it.coins + bonus,
                completedLevels = updatedSet
            )
        }

        saveCoinsToPrefs(context, _uiState.value.coins)
        saveCompletedLevelsToPrefs(context, updatedSet)
        clearActiveSessionFromPrefs(context, currentLvl)
    }

    fun purchaseExtraMoves(activity: Activity) {
        if (_uiState.value.coins >= 100) {
            _uiState.update {
                it.copy(
                    coins = it.coins - 100,
                    movesRemaining = 15,
                    showOutofMovesDialog = false
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
                    showOutofMovesDialog = false
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
        val completedSet = mutableSetOf<Int>()
        
        val allPrefs = prefs.all
        for ((key, _) in allPrefs) {
            if (key.startsWith("level_") && key.endsWith("_completed")) {
                val levelNumStr = key.substring(6, key.length - 10)
                val levelNum = levelNumStr.toIntOrNull()
                if (levelNum != null) {
                    completedSet.add(levelNum)
                }
            }
        }
        
        val savedCoins = prefs.getInt("user_coins", 100)
        val isAdFree = prefs.getBoolean("is_ad_free", false)
        _uiState.update { it.copy(completedLevels = completedSet, coins = savedCoins, isAdFree = isAdFree) }
    }

    fun openStore() {
        val current = _uiState.value.screenState
        val prev = if (current is ScreenState.Store) _uiState.value.previousScreenState else current
        _uiState.update { it.copy(previousScreenState = prev, screenState = ScreenState.Store) }
    }

    fun closeStore() {
        _uiState.update { it.copy(screenState = it.previousScreenState) }
    }

    fun toggleStoreDialog(show: Boolean) {
        if (show) openStore() else closeStore()
    }

    fun buyCoinPack(context: Context, coinAmount: Int) {
        val newCoins = _uiState.value.coins + coinAmount
        _uiState.update { it.copy(coins = newCoins) }
        saveCoinsToPrefs(context, newCoins)
    }

    fun buyRemoveAds(context: Context) {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_ad_free", true).apply()
        _uiState.update { it.copy(isAdFree = true) }
    }

    private fun saveCoinsToPrefs(context: Context, newCoins: Int) {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_coins", newCoins).apply()
    }

    private fun saveCompletedLevelsToPrefs(context: Context, completedLevels: Set<Int>) {
        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        completedLevels.forEach { lvl ->
            editor.putBoolean("level_${lvl}_completed", true)
        }
        editor.apply()
    }

    fun playLevel(levelNum: Int, activity: Activity) {
        val session = getSavedSession(activity, levelNum)
        if (session != null) {
            undoStack.clear()
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
                    totalWordsToMatch = session.levelData.targetWords.size,
                    totalMatchedWordsCount = session.totalMatchedWordsCount,
                    screenState = ScreenState.Gameplay,
                    selectedCardId = null,
                    shakingCardId = null
                )
            }
        } else {
            _uiState.update { it.copy(levelNumber = levelNum) }
            startNewGame(activity)
        }
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
            null
        }
    }

    private fun pushToUndoStack() {
        val state = _uiState.value
        val levelData = state.levelData ?: return
        if (undoStack.size >= 10) {
            undoStack.removeAt(0)
        }
        undoStack.add(
            SavedGameSession(
                levelNumber = state.levelNumber,
                levelData = levelData,
                foundationSlots = state.foundationSlots.map { it.copy() },
                tableauPiles = state.tableauPiles.map { col -> col.map { it.copy() } },
                stockPile = state.stockPile.map { it.copy() },
                wastePile = state.wastePile.map { it.copy() },
                score = state.score,
                movesRemaining = state.movesRemaining,
                totalMatchedWordsCount = state.totalMatchedWordsCount
            )
        )
    }

    fun undoLastMove(context: Context, onShowToast: (String) -> Unit) {
        if (undoStack.isEmpty()) {
            onShowToast("Geri alınacak hamle yok!")
            return
        }
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

        val hint = findPossibleMove()
        if (hint != null) {
            _uiState.update {
                it.copy(
                    hintedCardId = hint.first,
                    hintedTargetId = hint.second,
                    coins = maxOf(0, it.coins - 50)
                )
            }
            saveCoinsToPrefs(context, _uiState.value.coins)
            viewModelScope.launch {
                delay(3000)
                _uiState.update {
                    it.copy(
                        hintedCardId = null,
                        hintedTargetId = null
                    )
                }
            }
            onShowToast("İpucu gösteriliyor! (-50 🪙)")
        } else {
            onShowToast("Şu anda hamle yok, desteden kart çekmeyi deneyin!")
        }
    }

    fun useJoker(context: Context, onShowToast: (String) -> Unit) {
        val state = _uiState.value
        if (state.coins < 200) {
            onShowToast("Yetersiz altın! (200 🪙 gerekli)")
            return
        }

        pushToUndoStack()

        val jokerCard = SolitaireCard(
            id = "joker_${System.currentTimeMillis()}",
            text = "JOKER",
            categoryId = "joker_wildcard",
            isCategory = false,
            isFaceUp = true,
            word = Word("joker_word_${System.currentTimeMillis()}", "joker_wildcard", "JOKER", "Kolay")
        )

        val newWaste = state.wastePile + jokerCard

        _uiState.update {
            it.copy(
                wastePile = newWaste,
                coins = maxOf(0, it.coins - 200)
            )
        }

        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        onShowToast("Joker kartı çekildi! (-200 🪙)")
    }

    private fun findPossibleMove(): Pair<String, String>? {
        val state = _uiState.value
        
        val wasteTop = state.wastePile.lastOrNull()
        if (wasteTop != null) {
            for (slot in state.foundationSlots) {
                if (slot.activeCategory != null && wasteTop.categoryId == slot.activeCategory.id) {
                    return Pair(wasteTop.id, "slot_${slot.id}")
                }
            }
        }

        for ((cIdx, col) in state.tableauPiles.withIndex()) {
            val topCard = col.lastOrNull { it.isFaceUp } ?: continue
            for (slot in state.foundationSlots) {
                if (slot.activeCategory != null && topCard.categoryId == slot.activeCategory.id) {
                    return Pair(topCard.id, "slot_${slot.id}")
                }
            }
        }

        if (wasteTop != null) {
            for (slot in state.foundationSlots) {
                if (slot.activeCategory == null && wasteTop.isCategory) {
                    return Pair(wasteTop.id, "slot_${slot.id}")
                }
            }
        }

        for ((cIdx, col) in state.tableauPiles.withIndex()) {
            val card = col.lastOrNull { it.isFaceUp } ?: continue
            if (card.isCategory) {
                for (slot in state.foundationSlots) {
                    if (slot.activeCategory == null) {
                        return Pair(card.id, "slot_${slot.id}")
                    }
                }
            }
        }

        return null
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

    fun acceptDefeat() {
        _uiState.update {
            it.copy(
                showOutofMovesDialog = false,
                screenState = ScreenState.GameOver
            )
        }
    }
}
