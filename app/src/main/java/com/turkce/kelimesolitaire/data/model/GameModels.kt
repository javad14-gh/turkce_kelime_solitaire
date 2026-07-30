package com.turkce.kelimesolitaire.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val difficulty: String,
    val group: String? = null
)

@Serializable
data class Word(
    val id: String,
    val categoryId: String,
    val wordText: String,
    val difficulty: String
)

@Serializable
data class WordDatabase(
    val categories: List<Category>,
    val words: List<Word>
)

data class SolitaireCard(
    val id: String,
    val text: String,
    val categoryId: String,
    val isCategory: Boolean,
    val isFaceUp: Boolean,
    val word: Word? = null,
    val category: Category? = null
)

data class FoundationSlot(
    val id: Int,
    val activeCategory: Category? = null,
    val matchedWords: List<Word> = emptyList()
)

data class LevelData(
    val levelNumber: Int,
    val targetCategories: List<Category>,
    val targetWords: List<Word>,
    val initialTableau: List<List<SolitaireCard>>,
    val initialStock: List<SolitaireCard>
)
