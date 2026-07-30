package com.turkce.kelimesolitaire.domain

import com.turkce.kelimesolitaire.data.model.Category
import com.turkce.kelimesolitaire.data.model.LevelData
import com.turkce.kelimesolitaire.data.model.SolitaireCard
import com.turkce.kelimesolitaire.data.model.Word
import com.turkce.kelimesolitaire.data.model.WordDatabase
import kotlin.random.Random

class LevelGenerator {

    fun generateLevel(database: WordDatabase, levelNumber: Int): LevelData {
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
            targetCategories = defaultCategories,
            targetWords = defaultWords,
            initialTableau = listOf(cards, emptyList(), emptyList(), emptyList()),
            initialStock = emptyList()
        )
    }

    /**
     * Solvability check simulation engine.
     * Simulates card matches to verify if all word cards can be cleared.
     */
    private fun verifySolvability(
        totalWordsToMatch: Int,
        initialTableaus: List<List<SolitaireCard>>,
        initialStock: List<SolitaireCard>
    ): Boolean {
        // Clone board states for simulation
        val tableaus = initialTableaus.map { it.toMutableList() }
        val stock = initialStock.toMutableList()
        val waste = mutableListOf<SolitaireCard>()
        val activeCategories = mutableSetOf<String>()
        var matchedCount = 0

        var drawsSinceProgress = 0
        val maxDraws = maxOf(40, (stock.size + 1) * 3)

        while (matchedCount < totalWordsToMatch && drawsSinceProgress < maxDraws) {
            var moveMade = false

            // --- Move A: Check Tableau columns bottom cards ---
            for (colIdx in 0..3) {
                val colList = tableaus[colIdx]
                val bottomCard = colList.lastOrNull() ?: continue
                if (bottomCard.isFaceUp) {
                    if (bottomCard.isCategory) {
                        // Play Category Card to active foundations
                        activeCategories.add(bottomCard.categoryId)
                        colList.removeAt(colList.size - 1)
                        if (colList.isNotEmpty()) {
                            colList[colList.size - 1] = colList[colList.size - 1].copy(isFaceUp = true)
                        }
                        moveMade = true
                        drawsSinceProgress = 0 // progress reset
                        break
                    } else {
                        // Play Word Card to active categories
                        if (activeCategories.contains(bottomCard.categoryId)) {
                            matchedCount++
                            colList.removeAt(colList.size - 1)
                            if (colList.isNotEmpty()) {
                                colList[colList.size - 1] = colList[colList.size - 1].copy(isFaceUp = true)
                            }
                            moveMade = true
                            drawsSinceProgress = 0 // progress reset
                            break
                        }
                        
                        // Try stacking Tableau card to another column if categories match
                        for (targetColIdx in 0..3) {
                            if (targetColIdx == colIdx) continue
                            val targetBottom = tableaus[targetColIdx].lastOrNull()
                            if (targetBottom != null && targetBottom.isFaceUp && targetBottom.categoryId == bottomCard.categoryId) {
                                // Stacking matches. Move only if it uncovers a face-down card underneath
                                val hasFaceDownUnder = colList.size > 1 && !colList[colList.size - 2].isFaceUp
                                if (hasFaceDownUnder) {
                                    colList.removeAt(colList.size - 1)
                                    colList[colList.size - 1] = colList[colList.size - 1].copy(isFaceUp = true) // auto flip
                                    tableaus[targetColIdx].add(bottomCard)
                                    moveMade = true
                                    drawsSinceProgress = 0
                                    break
                                }
                            }
                        }
                        if (moveMade) break
                    }
                }
            }

            if (moveMade) continue

            // --- Move B: Check top Waste pile card ---
            val topWaste = waste.lastOrNull()
            if (topWaste != null) {
                if (topWaste.isCategory) {
                    activeCategories.add(topWaste.categoryId)
                    waste.removeAt(waste.size - 1)
                    moveMade = true
                    drawsSinceProgress = 0
                } else if (activeCategories.contains(topWaste.categoryId)) {
                    matchedCount++
                    waste.removeAt(waste.size - 1)
                    moveMade = true
                    drawsSinceProgress = 0
                } else {
                    // Try stacking Waste card onto a Tableau column
                    for (targetColIdx in 0..3) {
                        val targetBottom = tableaus[targetColIdx].lastOrNull()
                        if (targetBottom != null && targetBottom.isFaceUp && targetBottom.categoryId == topWaste.categoryId) {
                            waste.removeAt(waste.size - 1)
                            tableaus[targetColIdx].add(topWaste)
                            moveMade = true
                            drawsSinceProgress = 0
                            break
                        }
                    }
                }
            }

            if (moveMade) continue

            // --- Move C: Draw from stock ---
            if (stock.isNotEmpty()) {
                val drawn = stock.removeAt(0).copy(isFaceUp = true)
                waste.add(drawn)
                drawsSinceProgress++
            } else if (waste.isNotEmpty()) {
                // Recycle waste
                val recycled = waste.map { it.copy(isFaceUp = false) }
                stock.addAll(recycled)
                waste.clear()
                drawsSinceProgress++
            } else {
                // Both stock and waste are empty, and board not fully matched
                break
            }
        }

        return matchedCount >= totalWordsToMatch
    }
}
