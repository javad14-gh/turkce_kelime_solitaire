package com.turkce.kelimesolitaire.domain

import com.turkce.kelimesolitaire.data.model.Category
import com.turkce.kelimesolitaire.data.model.LevelData
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.data.model.Word
import com.turkce.kelimesolitaire.data.model.WordDatabase
import kotlin.random.Random

class LevelGenerator {

    fun generateLevel(
        database: WordDatabase,
        levelNumber: Int,
        lastUsedCategoryIds: Set<String> = emptySet()
    ): LevelData {
        val allCategories = database.categories
        val allWords = database.words

        // Gradual progression curve
        val difficultyLevel = when {
            levelNumber <= 3 -> "Kolay" // Tutorial / Warmup
            levelNumber in 4..7 -> when (levelNumber) {
                4 -> "Kolay"
                else -> "Orta"
            }
            levelNumber in 8..10 -> when (levelNumber) {
                8, 9 -> "Zor"
                else -> "CokZor" // Level 10 milestone
            }
            levelNumber in 11..20 -> {
                val cycle = (levelNumber - 11) % 10
                // 11:Orta, 12:Orta, 13:Zor, 14:Orta, 15:Zor, 16:Zor, 17:Zor, 18:Orta, 19:Zor, 20:CokZor
                when (cycle) {
                    0, 1, 3, 7 -> "Orta"
                    9 -> "CokZor"
                    else -> "Zor"
                }
            }
            else -> {
                // Levels 21+: Endgame challenge (mostly Zor and CokZor with occasional Orta breather)
                val cycle = (levelNumber - 21) % 10
                when (cycle) {
                    0, 4 -> "Orta"
                    2, 5, 7 -> "Zor"
                    else -> "CokZor"
                }
            }
        }

        val scaleFactor = when {
            levelNumber <= 8 -> 0
            levelNumber <= 20 -> 1
            else -> 2
        }
        val baseCategories = when (difficultyLevel) {
            "Kolay" -> 3
            "Orta" -> 4
            "Zor" -> 5
            else -> 6 // CokZor
        }
        val numCategories = minOf(7, baseCategories + scaleFactor)

        val wordsPerCategory = when (difficultyLevel) {
            "Kolay" -> 4
            "Orta" -> 5
            "Zor" -> 6
            else -> 7 // CokZor
        }

        val allowedDifficulties = when (difficultyLevel) {
            "Kolay" -> listOf("Kolay")
            "Orta" -> listOf("Kolay", "Orta")
            "Zor" -> listOf("Orta", "Zor")
            else -> listOf("Kolay", "Orta", "Zor") // CokZor uses any difficulty but with maximum size
        }

        // Filter and choose categories based on constraints
        var selectedCategories = allCategories.filter { it.difficulty in allowedDifficulties }

        // Avoid consecutive duplicate categories from last level if possible
        val nonRepeating = selectedCategories.filterNot { it.id in lastUsedCategoryIds }
        if (nonRepeating.size >= numCategories) {
            selectedCategories = nonRepeating
        }

        if (selectedCategories.size < numCategories) {
            selectedCategories = allCategories
        }

        var attempts = 0
        while (attempts < 200) {
            // Generate deterministic random seed using level number and attempt index
            val seed = levelNumber.toLong() + (attempts * 1000L)
            val random = Random(seed)

            val shuffledCats = selectedCategories.shuffled(random)
            val currentSelectedCats = mutableListOf<Category>()
            val occupiedGroups = mutableSetOf<String>()
            
            for (cat in shuffledCats) {
                if (currentSelectedCats.size >= numCategories) break
                val group = cat.group
                if (group == null || !occupiedGroups.contains(group)) {
                    currentSelectedCats.add(cat)
                    if (group != null) {
                        occupiedGroups.add(group)
                    }
                }
            }

            // Fallback if we couldn't satisfy uniqueness of groups
            if (currentSelectedCats.size < numCategories) {
                currentSelectedCats.clear()
                currentSelectedCats.addAll(shuffledCats.take(numCategories))
            }

            val targetWords = mutableListOf<Word>()

            for (category in currentSelectedCats) {
                val categoryWords = allWords.filter { it.categoryId == category.id }
                val sampledWords = if (categoryWords.size > wordsPerCategory) {
                    categoryWords.shuffled(random).take(wordsPerCategory)
                } else {
                    categoryWords
                }
                targetWords.addAll(sampledWords)
            }

            // Create initial deck of cards
            val cards = mutableListOf<SolitaireCard>()
            
            // Add Category Cards
            currentSelectedCats.forEach { cat ->
                cards.add(
                    SolitaireCard(
                        id = "cat_${cat.id}",
                        text = cat.name,
                        categoryId = cat.id,
                        isCategory = true,
                        isFaceUp = false,
                        category = cat
                    )
                )
            }

            // Add Word Cards
            targetWords.forEach { w ->
                cards.add(
                    SolitaireCard(
                        id = "word_${w.id}",
                        text = w.wordText,
                        categoryId = w.categoryId,
                        isCategory = false,
                        isFaceUp = false,
                        word = w
                    )
                )
            }

            // Shuffle full deck
            val shuffled = cards.shuffled(random)

            // Deal 4 Tableau columns and Stock (30% to stock, remaining to tableau)
            val tableaus = List(4) { mutableListOf<SolitaireCard>() }
            val stockSize = maxOf(5, (shuffled.size * 0.30).toInt())
            val tableauSize = shuffled.size - stockSize
            
            for (i in 0 until tableauSize) {
                val col = i % 4
                tableaus[col].add(shuffled[i])
            }

            // Flip bottom card of each Tableau column face-up
            for (col in 0..3) {
                val list = tableaus[col]
                if (list.isNotEmpty()) {
                    val lastIdx = list.size - 1
                    list[lastIdx] = list[lastIdx].copy(isFaceUp = true)
                }
            }

            // Remaining cards go to stock
            val stock = if (shuffled.size > tableauSize) shuffled.drop(tableauSize) else emptyList()

            // Run Solvability Simulation
            val categoryWordCounts = targetWords.groupBy { it.categoryId }.mapValues { it.value.size }
            if (verifySolvability(targetWords.size, categoryWordCounts, tableaus, stock)) {
                // Solvable configuration successfully generated!
                return LevelData(
                    levelNumber = levelNumber,
                    difficulty = difficultyLevel,
                    targetCategories = currentSelectedCats,
                    targetWords = targetWords,
                    initialTableau = tableaus,
                    initialStock = stock
                )
            }

            attempts++
        }

        // Fallback: If no layout passed validation, return the last generated deal as a fallback
        val defaultCategories = selectedCategories.take(numCategories)
        val defaultWords = allWords.filter { it.categoryId in defaultCategories.map { c -> c.id } }
        val cards = mutableListOf<SolitaireCard>()
        defaultCategories.forEach { cat ->
            cards.add(SolitaireCard("cat_${cat.id}", cat.name, cat.id, true, true, category = cat))
        }
        defaultWords.forEach { w ->
            cards.add(SolitaireCard("word_${w.id}", w.wordText, w.categoryId, false, true, word = w))
        }
        return LevelData(
            levelNumber = levelNumber,
            difficulty = difficultyLevel,
            targetCategories = defaultCategories,
            targetWords = defaultWords,
            initialTableau = listOf(cards, emptyList(), emptyList(), emptyList()),
            initialStock = emptyList()
        )
    }


    /**
     * Solvability check simulation engine using DFS Backtracking.
     * Caches intermediate visited states to avoid cycles and verifies if all word cards can be cleared.
     */
    private fun verifySolvability(
        totalWordsToMatch: Int,
        categoryWordCounts: Map<String, Int>,
        initialTableaus: List<List<SolitaireCard>>,
        initialStock: List<SolitaireCard>
    ): Boolean {
        val visited = mutableSetOf<String>()
        return solveDfs(
            tableaus = initialTableaus,
            stock = initialStock,
            waste = emptyList(),
            activeSlots = emptyMap(),
            completedCategories = emptySet(),
            matchedCount = 0,
            visited = visited,
            totalWordsToMatch = totalWordsToMatch,
            categoryWordCounts = categoryWordCounts
        )
    }

    private fun generateStateKey(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeSlots: Map<String, Int>
    ): String {
        val tStr = tableaus.joinToString(";") { col ->
            col.joinToString(",") { card ->
                "${card.id}:${if (card.isFaceUp) "U" else "D"}"
            }
        }
        val sStr = stock.joinToString(",") { it.id }
        val wStr = waste.joinToString(",") { it.id }
        val aStr = activeSlots.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
        return "T:$tStr|S:$sStr|W:$wStr|A:$aStr"
    }

    private fun solveDfs(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeSlots: Map<String, Int>, // maps active categoryId -> count of matched words (strictly size <= 4)
        completedCategories: Set<String>,
        matchedCount: Int,
        visited: MutableSet<String>,
        totalWordsToMatch: Int,
        categoryWordCounts: Map<String, Int>
    ): Boolean {
        if (matchedCount >= totalWordsToMatch) return true

        // Safety threshold limit to prevent UI hanging on complex layouts
        if (visited.size > 4000) return false

        val stateKey = generateStateKey(tableaus, stock, waste, activeSlots)
        if (visited.contains(stateKey)) return false
        visited.add(stateKey)

        // 1. Play bottom Tableau card to Foundation slots
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            val bottomCard = col.lastOrNull() ?: continue
            if (bottomCard.isFaceUp) {
                if (bottomCard.isCategory) {
                    // Category card can ONLY be placed if foundation slots have room (< 4) and category isn't already active/completed
                    if (activeSlots.size < 4 && !activeSlots.containsKey(bottomCard.categoryId) && !completedCategories.contains(bottomCard.categoryId)) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            if (idx == colIdx) {
                                val newList = list.toMutableList()
                                newList.removeAt(newList.size - 1)
                                if (newList.isNotEmpty()) {
                                    newList[newList.size - 1] = newList[newList.size - 1].copy(isFaceUp = true)
                                }
                                newList
                            } else list
                        }
                        val nextActiveSlots = activeSlots + (bottomCard.categoryId to 0)
                        if (solveDfs(nextTableaus, stock, waste, nextActiveSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                            return true
                        }
                    }
                } else {
                    // Word card can ONLY be placed if its category is currently active in one of the 4 slots
                    if (activeSlots.containsKey(bottomCard.categoryId)) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            if (idx == colIdx) {
                                val newList = list.toMutableList()
                                newList.removeAt(newList.size - 1)
                                if (newList.isNotEmpty()) {
                                    newList[newList.size - 1] = newList[newList.size - 1].copy(isFaceUp = true)
                                }
                                newList
                            } else list
                        }
                        val currentMatched = activeSlots[bottomCard.categoryId] ?: 0
                        val req = categoryWordCounts[bottomCard.categoryId] ?: 4
                        val newMatched = currentMatched + 1
                        val isComplete = newMatched >= req
                        // When category is completed, clear slot so a new category can be placed!
                        val nextActiveSlots = if (isComplete) activeSlots - bottomCard.categoryId else activeSlots + (bottomCard.categoryId to newMatched)
                        val nextCompleted = if (isComplete) completedCategories + bottomCard.categoryId else completedCategories

                        if (solveDfs(nextTableaus, stock, waste, nextActiveSlots, nextCompleted, matchedCount + 1, visited, totalWordsToMatch, categoryWordCounts)) {
                            return true
                        }
                    }
                }
            }
        }

        // 2. Play top Waste card to Foundation slots
        val topWaste = waste.lastOrNull()
        if (topWaste != null) {
            if (topWaste.isCategory) {
                if (activeSlots.size < 4 && !activeSlots.containsKey(topWaste.categoryId) && !completedCategories.contains(topWaste.categoryId)) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    val nextActiveSlots = activeSlots + (topWaste.categoryId to 0)
                    if (solveDfs(tableaus, stock, nextWaste, nextActiveSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                        return true
                    }
                }
            } else {
                if (activeSlots.containsKey(topWaste.categoryId)) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    val currentMatched = activeSlots[topWaste.categoryId] ?: 0
                    val req = categoryWordCounts[topWaste.categoryId] ?: 4
                    val newMatched = currentMatched + 1
                    val isComplete = newMatched >= req
                    val nextActiveSlots = if (isComplete) activeSlots - topWaste.categoryId else activeSlots + (topWaste.categoryId to newMatched)
                    val nextCompleted = if (isComplete) completedCategories + topWaste.categoryId else completedCategories

                    if (solveDfs(tableaus, stock, nextWaste, nextActiveSlots, nextCompleted, matchedCount + 1, visited, totalWordsToMatch, categoryWordCounts)) {
                        return true
                    }
                }
            }
        }

        // 3. Stacking Tableau card / group to another column (matching category OR empty column)
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            if (col.isEmpty()) continue

            val lastCard = col.last()
            if (!lastCard.isFaceUp) continue

            val targetCatId = lastCard.categoryId
            var startIdx = col.size - 1
            while (startIdx > 0) {
                val prevCard = col[startIdx - 1]
                if (!prevCard.isFaceUp || prevCard.categoryId != targetCatId) break
                startIdx--
            }

            val groupToMove = col.subList(startIdx, col.size)
            val hasFaceDownUnder = startIdx > 0 && !col[startIdx - 1].isFaceUp
            val hasDifferentFaceUpUnder = startIdx > 0 && col[startIdx - 1].isFaceUp && col[startIdx - 1].categoryId != targetCatId

            for (targetColIdx in 0..3) {
                if (targetColIdx == colIdx) continue
                val targetCol = tableaus[targetColIdx]

                // Option 3A: Target column is empty (can park group to reveal face-down card underneath)
                if (targetCol.isEmpty()) {
                    if (hasFaceDownUnder || hasDifferentFaceUpUnder) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            when (idx) {
                                colIdx -> {
                                    val newList = list.subList(0, startIdx).toMutableList()
                                    if (newList.isNotEmpty()) {
                                        val lIdx = newList.size - 1
                                        newList[lIdx] = newList[lIdx].copy(isFaceUp = true)
                                    }
                                    newList
                                }
                                targetColIdx -> groupToMove.toList()
                                else -> list
                            }
                        }
                        if (solveDfs(nextTableaus, stock, waste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                            return true
                        }
                    }
                } else {
                    // Option 3B: Target column has matching category on top
                    val targetBottom = targetCol.last()
                    if (targetBottom.isFaceUp && targetBottom.categoryId == targetCatId) {
                        if (hasFaceDownUnder || hasDifferentFaceUpUnder) {
                            val nextTableaus = tableaus.mapIndexed { idx, list ->
                                when (idx) {
                                    colIdx -> {
                                        val newList = list.subList(0, startIdx).toMutableList()
                                        if (newList.isNotEmpty()) {
                                            val lIdx = newList.size - 1
                                            newList[lIdx] = newList[lIdx].copy(isFaceUp = true)
                                        }
                                        newList
                                    }
                                    targetColIdx -> {
                                        val newList = list.toMutableList()
                                        newList.addAll(groupToMove)
                                        newList
                                    }
                                    else -> list
                                }
                            }
                            if (solveDfs(nextTableaus, stock, waste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                                return true
                            }
                        }
                    }
                }
            }
        }

        // 4. Stacking Waste card onto a Tableau column
        if (topWaste != null && !topWaste.isCategory) {
            for (targetColIdx in 0..3) {
                val targetCol = tableaus[targetColIdx]
                val targetBottom = targetCol.lastOrNull()
                val canStack = (targetBottom != null && targetBottom.isFaceUp && targetBottom.categoryId == topWaste.categoryId) ||
                               (targetBottom == null)
                if (canStack) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    val nextTableaus = tableaus.mapIndexed { idx, list ->
                        if (idx == targetColIdx) {
                            val newList = list.toMutableList()
                            newList.add(topWaste)
                            newList
                        } else list
                    }
                    if (solveDfs(nextTableaus, stock, nextWaste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                        return true
                    }
                }
            }
        }

        // 5. Draw from stock
        if (stock.isNotEmpty()) {
            val nextStock = stock.toMutableList()
            val drawn = nextStock.removeAt(nextStock.size - 1)
            val nextWaste = waste.toMutableList()
            nextWaste.add(drawn.copy(isFaceUp = true))
            if (solveDfs(tableaus, nextStock, nextWaste, activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                return true
            }
        }

        // 6. Recycle waste back to stock
        if (stock.isEmpty() && waste.isNotEmpty()) {
            val nextStock = waste.reversed().map { it.copy(isFaceUp = false) }
            if (solveDfs(tableaus, nextStock, emptyList(), activeSlots, completedCategories, matchedCount, visited, totalWordsToMatch, categoryWordCounts)) {
                return true
            }
        }

        return false
    }
}
