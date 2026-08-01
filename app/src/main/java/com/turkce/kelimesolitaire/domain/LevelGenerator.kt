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

        // Determine level configuration parameters based on a repeating 10-level cycle
        val cycleIndex = (levelNumber - 1) % 10
        // Cycle Pattern: 0:Easy, 1:Easy, 2:Medium, 3:Easy, 4:Medium, 5:Medium, 6:Hard, 7:Easy, 8:Medium, 9:VeryHard
        val difficultyLevel = when (cycleIndex) {
            0, 1, 3, 7 -> "Kolay"
            2, 4, 5, 8 -> "Orta"
            6 -> "Zor"
            else -> "CokZor"
        }

        val scaleFactor = (levelNumber - 1) / 30
        val baseCategories = when (difficultyLevel) {
            "Kolay" -> 3
            "Orta" -> 4
            "Zor" -> 5
            else -> 6 // CokZor
        }
        val numCategories = minOf(7, baseCategories + scaleFactor)

        val wordsPerCategory = when (difficultyLevel) {
            "Kolay" -> 4
            "Orta" -> 4
            "Zor" -> 4
            else -> 5 // CokZor
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

            // Deal 4 Tableau columns (cascaded piles, up to 16 cards)
            val tableaus = List(4) { mutableListOf<SolitaireCard>() }
            val tableauSize = if (shuffled.size > 8) shuffled.size - 6 else shuffled.size / 2
            
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
            if (verifySolvability(targetWords.size, tableaus, stock)) {
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
        initialTableaus: List<List<SolitaireCard>>,
        initialStock: List<SolitaireCard>
    ): Boolean {
        val visited = mutableSetOf<String>()
        return solveDfs(
            tableaus = initialTableaus,
            stock = initialStock,
            waste = emptyList(),
            activeCategories = emptySet(),
            matchedCount = 0,
            visited = visited,
            totalWordsToMatch = totalWordsToMatch
        )
    }

    private fun generateStateKey(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeCategories: Set<String>
    ): String {
        val tStr = tableaus.joinToString(";") { col ->
            col.joinToString(",") { card ->
                "${card.id}:${if (card.isFaceUp) "U" else "D"}"
            }
        }
        val sStr = stock.joinToString(",") { it.id }
        val wStr = waste.joinToString(",") { it.id }
        val aStr = activeCategories.sorted().joinToString(",")
        return "T:$tStr|S:$sStr|W:$wStr|A:$aStr"
    }

    private fun solveDfs(
        tableaus: List<List<SolitaireCard>>,
        stock: List<SolitaireCard>,
        waste: List<SolitaireCard>,
        activeCategories: Set<String>,
        matchedCount: Int,
        visited: MutableSet<String>,
        totalWordsToMatch: Int
    ): Boolean {
        if (matchedCount >= totalWordsToMatch) return true

        // Safety threshold limit to prevent UI hanging on complex unsolvable configurations
        if (visited.size > 2000) return false

        val stateKey = generateStateKey(tableaus, stock, waste, activeCategories)
        if (visited.contains(stateKey)) return false
        visited.add(stateKey)

        // Try all valid moves:

        // 1. Play bottom Tableau card to slots
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            val bottomCard = col.lastOrNull() ?: continue
            if (bottomCard.isFaceUp) {
                if (bottomCard.isCategory) {
                    if (!activeCategories.contains(bottomCard.categoryId)) {
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
                        if (solveDfs(nextTableaus, stock, waste, activeCategories + bottomCard.categoryId, matchedCount, visited, totalWordsToMatch)) {
                            return true
                        }
                    }
                } else {
                    if (activeCategories.contains(bottomCard.categoryId)) {
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
                        if (solveDfs(nextTableaus, stock, waste, activeCategories, matchedCount + 1, visited, totalWordsToMatch)) {
                            return true
                        }
                    }
                }
            }
        }

        // 2. Play top Waste card to slots
        val topWaste = waste.lastOrNull()
        if (topWaste != null) {
            if (topWaste.isCategory) {
                if (!activeCategories.contains(topWaste.categoryId)) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    if (solveDfs(tableaus, stock, nextWaste, activeCategories + topWaste.categoryId, matchedCount, visited, totalWordsToMatch)) {
                        return true
                    }
                }
            } else {
                if (activeCategories.contains(topWaste.categoryId)) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    if (solveDfs(tableaus, stock, nextWaste, activeCategories, matchedCount + 1, visited, totalWordsToMatch)) {
                        return true
                    }
                }
            }
        }

        // 3. Stacking Tableau card to another column if categories match
        for (colIdx in 0..3) {
            val col = tableaus[colIdx]
            val bottomCard = col.lastOrNull() ?: continue
            if (bottomCard.isFaceUp && !bottomCard.isCategory) {
                for (targetColIdx in 0..3) {
                    if (targetColIdx == colIdx) continue
                    val targetBottom = tableaus[targetColIdx].lastOrNull()
                    val hasFaceDownUnder = col.size > 1 && !col[col.size - 2].isFaceUp
                    if (hasFaceDownUnder && targetBottom != null && targetBottom.isFaceUp && targetBottom.categoryId == bottomCard.categoryId) {
                        val nextTableaus = tableaus.mapIndexed { idx, list ->
                            when (idx) {
                                colIdx -> {
                                    val newList = list.toMutableList()
                                    newList.removeAt(newList.size - 1)
                                    newList[newList.size - 1] = newList[newList.size - 1].copy(isFaceUp = true)
                                    newList
                                }
                                targetColIdx -> {
                                    val newList = list.toMutableList()
                                    newList.add(bottomCard)
                                    newList
                                }
                                else -> list
                            }
                        }
                        if (solveDfs(nextTableaus, stock, waste, activeCategories, matchedCount, visited, totalWordsToMatch)) {
                            return true
                        }
                    }
                }
            }
        }

        // 4. Stacking Waste card onto a Tableau column
        if (topWaste != null && !topWaste.isCategory) {
            for (targetColIdx in 0..3) {
                val targetBottom = tableaus[targetColIdx].lastOrNull()
                if (targetBottom != null && targetBottom.isFaceUp && targetBottom.categoryId == topWaste.categoryId) {
                    val nextWaste = waste.toMutableList()
                    nextWaste.removeAt(nextWaste.size - 1)
                    val nextTableaus = tableaus.mapIndexed { idx, list ->
                        if (idx == targetColIdx) {
                            val newList = list.toMutableList()
                            newList.add(topWaste)
                            newList
                        } else list
                    }
                    if (solveDfs(nextTableaus, stock, nextWaste, activeCategories, matchedCount, visited, totalWordsToMatch)) {
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
            if (solveDfs(tableaus, nextStock, nextWaste, activeCategories, matchedCount, visited, totalWordsToMatch)) {
                return true
            }
        }

        // 6. Recycle waste back to stock
        if (stock.isEmpty() && waste.isNotEmpty()) {
            val nextStock = waste.reversed().map { it.copy(isFaceUp = false) }
            if (solveDfs(tableaus, nextStock, emptyList(), activeCategories, matchedCount, visited, totalWordsToMatch)) {
                return true
            }
        }
        return false
    }
}
