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
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardManager
import com.turkce.kelimesolitaire.data.dailyreward.DailyRewardState

sealed interface ScreenState {
    object MainMenu : ScreenState
    object Loading : ScreenState
    object Gameplay : ScreenState
    object LevelComplete : ScreenState
    object GameOver : ScreenState // Defeat screen
    object Store : ScreenState // Full-screen store
}

enum class TutorialType {
    LEVEL_1_GUIDE,
    UNDO_UNLOCK,
    HINT_UNLOCK,
    JOKER_UNLOCK
}

data class GameUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val previousScreenState: ScreenState = ScreenState.MainMenu,
    val levelNumber: Int = 1,
    val score: Int = 0,
    val coins: Int = 50,
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
    val showStoreDialog: Boolean = false,
    val showDailyRewardDialog: Boolean = false,
    val dailyRewardState: DailyRewardState? = null,
    val dailyRewardHasUnclaimed: Boolean = false,

    // Booster progressive unlocks & free gifts
    val isUndoUnlocked: Boolean = false,
    val isHintUnlocked: Boolean = false,
    val isJokerUnlocked: Boolean = false,
    val hasFreeUndo: Boolean = false,
    val hasFreeHint: Boolean = false,
    val hasFreeJoker: Boolean = false,
    val activeTutorial: TutorialType? = null,
    val level1TutorialStep: Int = 0
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
        
        if (!_uiState.value.isAdFree && currentLvl > 1 && currentLvl % 2 == 0) {
            adManager.showInterstitial(activity) {
                loadLevelData(currentLvl, db, activity)
                saveActiveSessionToPrefs(activity)
            }
        } else {
            loadLevelData(currentLvl, db, activity)
            saveActiveSessionToPrefs(activity)
        }
    }

    private fun loadLevelData(levelNum: Int, db: WordDatabase, context: Context) {
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

        val baseMoves = allWords.size + generated.targetCategories.size + generated.initialStock.size
        val (bufferRatio, minBuffer) = when (generated.difficulty) {
            "Kolay" -> Pair(0.25, 4)
            "Orta"  -> Pair(0.35, 6)
            "Zor"   -> Pair(0.45, 8)
            else    -> Pair(0.50, 10) // CokZor
        }
        val bufferMoves = maxOf(minBuffer, (baseMoves * bufferRatio).toInt())
        val calculatedMoves = baseMoves + bufferMoves

        val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)

        val isUndoUnlocked = levelNum >= 2
        val isHintUnlocked = levelNum >= 8
        val isJokerUnlocked = levelNum >= 10

        var hasFreeUndo = prefs.getBoolean("has_free_undo", false)
        var hasFreeHint = prefs.getBoolean("has_free_hint", false)
        var hasFreeJoker = prefs.getBoolean("has_free_joker", false)

        var activeTutorial: TutorialType? = null

        // Check Level 1 interactive guide
        if (levelNum == 1 && !prefs.getBoolean("tutorial_lvl1_completed", false)) {
            activeTutorial = TutorialType.LEVEL_1_GUIDE
        }

        // Check Undo tutorial (Level 2)
        if (levelNum >= 2 && !prefs.getBoolean("tutorial_undo_seen", false)) {
            activeTutorial = TutorialType.UNDO_UNLOCK
            hasFreeUndo = true
            prefs.edit()
                .putBoolean("tutorial_undo_seen", true)
                .putBoolean("has_free_undo", true)
                .apply()
        }

        // Check Hint tutorial (Level 8 - first Hard level)
        if (levelNum >= 8 && !prefs.getBoolean("tutorial_hint_seen", false)) {
            activeTutorial = TutorialType.HINT_UNLOCK
            hasFreeHint = true
            prefs.edit()
                .putBoolean("tutorial_hint_seen", true)
                .putBoolean("has_free_hint", true)
                .apply()
        }

        // Check Joker tutorial (Level 10 - first Very Hard level)
        if (levelNum >= 10 && !prefs.getBoolean("tutorial_joker_seen", false)) {
            activeTutorial = TutorialType.JOKER_UNLOCK
            hasFreeJoker = true
            prefs.edit()
                .putBoolean("tutorial_joker_seen", true)
                .putBoolean("has_free_joker", true)
                .apply()
        }

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
                movesRemaining = calculatedMoves,
                selectedCardId = null,
                shakingCardId = null,
                errorsInLevel = 0,
                isUndoUnlocked = isUndoUnlocked,
                isHintUnlocked = isHintUnlocked,
                isJokerUnlocked = isJokerUnlocked,
                hasFreeUndo = hasFreeUndo,
                hasFreeHint = hasFreeHint,
                hasFreeJoker = hasFreeJoker,
                activeTutorial = activeTutorial,
                level1TutorialStep = 0
            )
        }
    }

    fun dismissTutorial(context: Context) {
        val currentTut = _uiState.value.activeTutorial
        if (currentTut == TutorialType.LEVEL_1_GUIDE) {
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("tutorial_lvl1_completed", true).apply()
        }
        _uiState.update { it.copy(activeTutorial = null) }
    }

    fun selectCard(cardId: String?) {
        _uiState.update { it.copy(selectedCardId = cardId) }
    }

    fun drawFromStock(context: Context) {
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return
        }
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
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return false
        }

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

        // 1. Process category card first if present in the batch
        val categoryCardInBatch = cards.find { it.isCategory }
        if (categoryCardInBatch != null) {
            if (tempActiveCategory == null) {
                val resolvedCategory = categoryCardInBatch.category 
                    ?: _uiState.value.levelData?.targetCategories?.find { it.id == categoryCardInBatch.categoryId }
                if (resolvedCategory != null) {
                    tempActiveCategory = resolvedCategory
                    successCount++
                    scoreDelta += 10
                } else {
                    triggerShakeError(cards.firstOrNull()?.id)
                    return false
                }
            } else if (tempActiveCategory.id == categoryCardInBatch.categoryId) {
                // Category already active and matches
                successCount++
            } else {
                // Slot has a different category
                triggerShakeError(cards.firstOrNull()?.id)
                return false
            }
        }

        // 2. Process word cards
        val wordCards = cards.filter { !it.isCategory }
        for (card in wordCards) {
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
                }
            } else {
                break
            }
        }

        if (successCount == cards.size) {
            val newSlot = activeSlot.copy(activeCategory = tempActiveCategory, matchedWords = tempMatchedWords)
            updatedSlots[slotIdx] = newSlot

            val (newTableaus, newWaste) = removeCardsFromSource(cards)
            val newTotalMatched = _uiState.value.totalMatchedWordsCount + newlyMatchedWordsCount

            val nextTutStep = if (_uiState.value.levelNumber == 1 && _uiState.value.activeTutorial == TutorialType.LEVEL_1_GUIDE) {
                if (cards.any { it.isCategory }) 1 else 2
            } else {
                _uiState.value.level1TutorialStep
            }

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
                    selectedCardId = null,
                    level1TutorialStep = nextTutStep
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
            } else {
                checkMovesRemaining()
            }
            return true
        } else {
            triggerShakeError(cards.firstOrNull()?.id)
            return false
        }
    }

    fun attemptStackCards(cards: List<SolitaireCard>, targetColIdx: Int, context: Context): Boolean {
        if (cards.isEmpty()) return false
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return false
        }
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
                // If destination card is a Category card, NO cards can be stacked on top of it!
                if (destinationBottomCard.isCategory) {
                    return false
                }

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
        val bonus = if (isReplay) 0 else when (_uiState.value.levelData?.difficulty) {
            "Kolay" -> 10
            "Orta" -> 15
            "Zor" -> 25
            "CokZor" -> 35
            else -> 15
        }

        val updatedSet = _uiState.value.completedLevels + currentLvl

        if (currentLvl == 1) {
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("tutorial_lvl1_completed", true).apply()
        }

        _uiState.update {
            it.copy(
                screenState = ScreenState.LevelComplete,
                levelCompletedBonus = bonus,
                coins = it.coins + bonus,
                completedLevels = updatedSet,
                activeTutorial = null
            )
        }

        saveCoinsToPrefs(context, _uiState.value.coins)
        saveCompletedLevelsToPrefs(context, updatedSet)
        clearActiveSessionFromPrefs(context, currentLvl)
    }

    fun purchaseExtraMoves(activity: Activity) {
        if (_uiState.value.coins >= 75) {
            _uiState.update {
                it.copy(
                    coins = it.coins - 75,
                    movesRemaining = 5,
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
                    movesRemaining = 5,
                    showOutofMovesDialog = false
                )
            }
            saveActiveSessionToPrefs(activity)
        }
    }

    fun restartLevel(activity: Activity) {
        val db = wordDatabase ?: return
        loadLevelData(_uiState.value.levelNumber, db, activity)
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
        
        val savedCoins = prefs.getInt("user_coins", 50)
        val isAdFree = prefs.getBoolean("is_ad_free", false)
        val dailyReward = DailyRewardManager.getDailyRewardState(context)
        _uiState.update { 
            it.copy(
                completedLevels = completedSet, 
                coins = savedCoins, 
                isAdFree = isAdFree,
                dailyRewardState = dailyReward,
                dailyRewardHasUnclaimed = dailyReward.isReadyToClaimToday,
                showDailyRewardDialog = dailyReward.isReadyToClaimToday
            ) 
        }
    }

    fun openDailyRewardDialog(context: Context) {
        val dailyReward = DailyRewardManager.getDailyRewardState(context)
        _uiState.update {
            it.copy(
                dailyRewardState = dailyReward,
                dailyRewardHasUnclaimed = dailyReward.isReadyToClaimToday,
                showDailyRewardDialog = true
            )
        }
    }

    fun dismissDailyRewardDialog() {
        _uiState.update { it.copy(showDailyRewardDialog = false) }
    }

    fun claimDailyReward(activity: Activity, doubleReward: Boolean, onShowToast: (String) -> Unit = {}) {
        val isPersian = LocaleHelper.isPersian(activity)
        if (doubleReward) {
            adManager.showRewarded(activity) { _ ->
                executeDailyClaim(activity, doubleReward = true, isPersian = isPersian, onShowToast = onShowToast)
            }
        } else {
            executeDailyClaim(activity, doubleReward = false, isPersian = isPersian, onShowToast = onShowToast)
        }
    }

    private fun executeDailyClaim(context: Context, doubleReward: Boolean, isPersian: Boolean, onShowToast: (String) -> Unit) {
        val awarded = DailyRewardManager.claimDailyReward(context, doubleReward)
        if (awarded > 0) {
            val newCoins = _uiState.value.coins + awarded
            val updatedDailyState = DailyRewardManager.getDailyRewardState(context)
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    dailyRewardState = updatedDailyState,
                    dailyRewardHasUnclaimed = false
                )
            }
            saveCoinsToPrefs(context, newCoins)
            val msg = if (isPersian) "+${LocaleHelper.formatNumber(awarded, true)} سکه دریافت شد!\u200F" else "+$awarded Altın kazanıldı!"
            onShowToast(msg)
        }
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

    fun toggleDailyRewardDialog(show: Boolean) {
        _uiState.update { it.copy(showDailyRewardDialog = show) }
    }

    fun addCoins(context: Context, amount: Int) {
        val newCoins = _uiState.value.coins + amount
        _uiState.update { it.copy(coins = newCoins) }
        saveCoinsToPrefs(context, newCoins)
    }

    fun buyCoinPack(context: Context, amount: Int) {
        addCoins(context, amount)
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
            val prefs = activity.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            val isUndoUnlocked = session.levelNumber >= 2
            val isHintUnlocked = session.levelNumber >= 8
            val isJokerUnlocked = session.levelNumber >= 10
            val hasFreeUndo = prefs.getBoolean("has_free_undo", false)
            val hasFreeHint = prefs.getBoolean("has_free_hint", false)
            val hasFreeJoker = prefs.getBoolean("has_free_joker", false)

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
                    shakingCardId = null,
                    isUndoUnlocked = isUndoUnlocked,
                    isHintUnlocked = isHintUnlocked,
                    isJokerUnlocked = isJokerUnlocked,
                    hasFreeUndo = hasFreeUndo,
                    hasFreeHint = hasFreeHint,
                    hasFreeJoker = hasFreeJoker,
                    activeTutorial = null
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
        val isPersian = LocaleHelper.isPersian(context)
        if (!_uiState.value.isUndoUnlocked) {
            onShowToast(if (isPersian) "قابلیت بازگشت در مرحله ۲ باز می‌شود!\u200F" else "Geri Al özelliği 2. seviyede açılır!")
            return
        }
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return
        }
        if (undoStack.isEmpty()) {
            onShowToast(if (isPersian) "حرکتی برای بازگشت وجود ندارد!\u200F" else "Geri alınacak hamle yok!")
            return
        }
        val state = _uiState.value
        val isFree = state.hasFreeUndo
        if (!isFree && state.coins < 50) {
            onShowToast(if (isPersian) "سکه ناکافی! (۵۰ 🪙 نیاز است)\u200F" else "Yetersiz altın! (50 🪙 gerekli)")
            return
        }
        val prevSession = undoStack.removeAt(undoStack.size - 1)
        val newCoins = if (isFree) state.coins else maxOf(0, state.coins - 50)
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
                coins = newCoins,
                hasFreeUndo = false,
                selectedCardId = null,
                shakingCardId = null
            )
        }
        if (isFree) {
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("has_free_undo", false).apply()
        }
        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        val toastMsg = if (isFree) {
            if (isPersian) "حرکت با استفاده از هدیه رایگان بازگردانده شد! 🎁\u200F" else "Geri alma ücretsiz kullanıldı! 🎁"
        } else {
            if (isPersian) "حرکت بازگردانده شد! (-۵۰ 🪙)\u200F" else "Geri alındı! (-50 🪙)"
        }
        onShowToast(toastMsg)
    }

    fun showHint(context: Context, onShowToast: (String) -> Unit) {
        val isPersian = LocaleHelper.isPersian(context)
        if (!_uiState.value.isHintUnlocked) {
            onShowToast(if (isPersian) "قابلیت راهنما در مرحله ۸ (سخت) باز می‌شود!\u200F" else "İpucu özelliği 8. seviyede açılır!")
            return
        }
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return
        }
        val state = _uiState.value
        val isFree = state.hasFreeHint
        if (!isFree && state.coins < 50) {
            onShowToast(if (isPersian) "سکه ناکافی! (۵۰ 🪙 نیاز است)\u200F" else "Yetersiz altın! (50 🪙 gerekli)")
            return
        }

        val hint = findPossibleMove()
        if (hint != null) {
            val newCoins = if (isFree) state.coins else maxOf(0, state.coins - 50)
            _uiState.update {
                it.copy(
                    hintedCardId = hint.first,
                    hintedTargetId = hint.second,
                    coins = newCoins,
                    hasFreeHint = false
                )
            }
            if (isFree) {
                val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("has_free_hint", false).apply()
            }
            saveCoinsToPrefs(context, _uiState.value.coins)
            viewModelScope.launch {
                delay(4000)
                _uiState.update {
                    it.copy(
                        hintedCardId = null,
                        hintedTargetId = null
                    )
                }
            }
            if (isFree) {
                onShowToast(if (isPersian) "راهنمایی رایگان فعال شد! 🎁\u200F" else "Ücretsiz ipucu kullanıldı! 🎁")
            } else if (hint.first == "stock_pile") {
                onShowToast(if (isPersian) "روی دسته کارت بزنید و کارت بکشید! (-۵۰ 🪙)\u200F" else "Desteden kart çekin! (-50 🪙)")
            } else {
                onShowToast(if (isPersian) "کارت و جایگاه مناسب با رنگ طلایی درخشان مشخص شدند! (-۵۰ 🪙)\u200F" else "Kart ve hedef altın çerçeveyle gösterildi! (-50 🪙)")
            }
        } else {
            onShowToast(if (isPersian) "هیچ حرکتی ممکن نیست! می‌توانید از جوکر استفاده کنید.\u200F" else "Hamle kalmadı! Joker kartını deneyin.")
        }
    }

    fun useJoker(context: Context, onShowToast: (String) -> Unit) {
        val isPersian = LocaleHelper.isPersian(context)
        if (!_uiState.value.isJokerUnlocked) {
            onShowToast(if (isPersian) "کارت جوکر در مرحله ۱۰ (خیلی سخت) باز می‌شود!\u200F" else "Joker kartı 10. seviyede açılır!")
            return
        }
        if (_uiState.value.movesRemaining <= 0 || _uiState.value.showOutofMovesDialog) {
            checkMovesRemaining()
            return
        }
        val state = _uiState.value
        val isFree = state.hasFreeJoker
        if (!isFree && state.coins < 200) {
            onShowToast(if (isPersian) "سکه ناکافی! (۲۰۰ 🪙 نیاز است)\u200F" else "Yetersiz altın! (200 🪙 gerekli)")
            return
        }

        pushToUndoStack()

        val jokerCard = SolitaireCard(
            id = "joker_${System.currentTimeMillis()}",
            text = if (isPersian) "جوکر" else "JOKER",
            categoryId = "joker_wildcard",
            isCategory = false,
            isFaceUp = true,
            word = Word("joker_word_${System.currentTimeMillis()}", "joker_wildcard", if (isPersian) "جوکر" else "JOKER", "Kolay")
        )

        val newWaste = state.wastePile + jokerCard
        val newCoins = if (isFree) state.coins else maxOf(0, state.coins - 200)

        _uiState.update {
            it.copy(
                wastePile = newWaste,
                coins = newCoins,
                hasFreeJoker = false
            )
        }

        if (isFree) {
            val prefs = context.getSharedPreferences("kelime_solitaire_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("has_free_joker", false).apply()
        }

        saveCoinsToPrefs(context, _uiState.value.coins)
        saveActiveSessionToPrefs(context)
        val toastMsg = if (isFree) {
            if (isPersian) "کارت جوکر با هدیه رایگان کشیده شد! 🃏🎁\u200F" else "Ücretsiz Joker kartı çekildi! 🃏🎁"
        } else {
            if (isPersian) "کارت جوکر کشیده شد! (-۲۰۰ 🪙)\u200F" else "Joker kartı çekildi! (-200 🪙)"
        }
        onShowToast(toastMsg)
    }

    private fun findPossibleMove(): Pair<String, String>? {
        val state = _uiState.value

        // 1. DIRECT MATCHES TO FOUNDATION
        // A) Category cards in Tableau to empty Foundation slot
        val emptySlot = state.foundationSlots.firstOrNull { it.activeCategory == null }
        if (emptySlot != null) {
            for (col in state.tableauPiles) {
                val catCard = col.find { it.isFaceUp && it.isCategory }
                if (catCard != null) {
                    return Pair(catCard.id, "slot_${emptySlot.id}")
                }
            }
            val wasteTop = state.wastePile.lastOrNull()
            if (wasteTop != null && wasteTop.isCategory) {
                return Pair(wasteTop.id, "slot_${emptySlot.id}")
            }
        }

        // B) Word cards in Tableau to active Foundation slot
        for (col in state.tableauPiles) {
            val faceUpCards = col.filter { it.isFaceUp }
            if (faceUpCards.isEmpty()) continue

            val topCard = faceUpCards.lastOrNull()
            if (topCard != null && !topCard.isCategory) {
                for (slot in state.foundationSlots) {
                    if (slot.activeCategory != null && 
                        (topCard.categoryId == slot.activeCategory.id || topCard.categoryId == "joker_wildcard")) {
                        return Pair(topCard.id, "slot_${slot.id}")
                    }
                }
            }

            // Check if full face-up group can move to active slot
            val firstFaceUp = faceUpCards.firstOrNull()
            if (firstFaceUp != null && !firstFaceUp.isCategory) {
                for (slot in state.foundationSlots) {
                    if (slot.activeCategory != null && 
                        (firstFaceUp.categoryId == slot.activeCategory.id || firstFaceUp.categoryId == "joker_wildcard")) {
                        val allMatch = faceUpCards.all { it.categoryId == slot.activeCategory.id || it.categoryId == "joker_wildcard" }
                        if (allMatch) {
                            return Pair(firstFaceUp.id, "slot_${slot.id}")
                        }
                    }
                }
            }
        }

        // C) Waste card to active Foundation slot
        val wasteTop = state.wastePile.lastOrNull()
        if (wasteTop != null && !wasteTop.isCategory) {
            for (slot in state.foundationSlots) {
                if (slot.activeCategory != null && 
                    (wasteTop.categoryId == slot.activeCategory.id || wasteTop.categoryId == "joker_wildcard")) {
                    return Pair(wasteTop.id, "slot_${slot.id}")
                }
            }
        }

        // 2. TABLEAU-TO-TABLEAU MOVES
        for (sourceIdx in 0..3) {
            val sourceCol = state.tableauPiles[sourceIdx]
            val faceUpCards = sourceCol.filter { it.isFaceUp }
            if (faceUpCards.isEmpty()) continue

            val firstFaceUp = faceUpCards.first()

            for (targetIdx in 0..3) {
                if (sourceIdx == targetIdx) continue
                val targetCol = state.tableauPiles[targetIdx]

                if (targetCol.isEmpty()) {
                    // Moving to empty column is high priority if it uncovers a face-down card
                    val hasFaceDown = sourceCol.any { !it.isFaceUp }
                    if (hasFaceDown) {
                        return Pair(firstFaceUp.id, "col_$targetIdx")
                    }
                } else {
                    val targetBottom = targetCol.last()
                    if (targetBottom.isFaceUp && !targetBottom.isCategory) {
                        val canStack = firstFaceUp.categoryId == targetBottom.categoryId || 
                                       firstFaceUp.categoryId == "joker_wildcard" || 
                                       targetBottom.categoryId == "joker_wildcard"
                        if (canStack) {
                            return Pair(firstFaceUp.id, "col_$targetIdx")
                        }
                    }
                }
            }

            // Also check moving just the last card of sourceCol if group has > 1 card
            if (faceUpCards.size > 1) {
                val lastCard = faceUpCards.last()
                for (targetIdx in 0..3) {
                    if (sourceIdx == targetIdx) continue
                    val targetCol = state.tableauPiles[targetIdx]
                    if (targetCol.isNotEmpty()) {
                        val targetBottom = targetCol.last()
                        if (targetBottom.isFaceUp && !targetBottom.isCategory) {
                            val canStack = lastCard.categoryId == targetBottom.categoryId || 
                                           lastCard.categoryId == "joker_wildcard" || 
                                           targetBottom.categoryId == "joker_wildcard"
                            if (canStack) {
                                return Pair(lastCard.id, "col_$targetIdx")
                            }
                        }
                    }
                }
            }
        }

        // 3. WASTE-TO-TABLEAU MOVES
        if (wasteTop != null) {
            for (targetIdx in 0..3) {
                val targetCol = state.tableauPiles[targetIdx]
                if (targetCol.isEmpty()) {
                    return Pair(wasteTop.id, "col_$targetIdx")
                } else {
                    val targetBottom = targetCol.last()
                    if (targetBottom.isFaceUp && !targetBottom.isCategory) {
                        val canStack = wasteTop.categoryId == targetBottom.categoryId || 
                                       wasteTop.categoryId == "joker_wildcard" || 
                                       targetBottom.categoryId == "joker_wildcard"
                        if (canStack) {
                            return Pair(wasteTop.id, "col_$targetIdx")
                        }
                    }
                }
            }
        }

        // 4. DRAW FROM STOCK PILE
        if (state.stockPile.isNotEmpty() || state.wastePile.isNotEmpty()) {
            return Pair("stock_pile", "stock_pile")
        }

        return null
    }

    fun buyExtraMoves(context: Context, onShowToast: (String) -> Unit) {
        val isPersian = LocaleHelper.isPersian(context)
        val state = _uiState.value
        if (state.coins < 75) {
            onShowToast(if (isPersian) "سکه ناکافی! (۷۵ 🪙 نیاز است)\u200F" else "Yetersiz altın! (75 🪙 gerekli)")
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
        onShowToast(if (isPersian) "۵ فرصت اضافه دریافت شد! (-۷۵ 🪙)\u200F" else "5 Ek Hamle alındı! (-75 🪙)")
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
