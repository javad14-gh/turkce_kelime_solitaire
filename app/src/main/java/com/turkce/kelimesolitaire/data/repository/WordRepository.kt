package com.turkce.kelimesolitaire.data.repository

import android.content.Context
import com.turkce.kelimesolitaire.data.model.WordDatabase
import kotlinx.serialization.json.Json
import java.io.IOException

class WordRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun loadDatabase(context: Context): WordDatabase? {
        val isPersianFlavor = context.packageName.contains("persian", ignoreCase = true) ||
                (try {
                    val buildConfigClass = Class.forName("com.turkce.kelimesolitaire.BuildConfig")
                    val flavorField = buildConfigClass.getField("FLAVOR")
                    flavorField.get(null) == "bazaar"
                } catch (e: Exception) {
                    false
                })

        val targetFileName = if (isPersianFlavor) "word_database_fa.json" else "word_database.json"
        val fallbackFileName = if (isPersianFlavor) "word_database.json" else "word_database_fa.json"

        val assetNames = listOf(targetFileName, fallbackFileName)
        for (fileName in assetNames) {
            try {
                val jsonString = context.assets.open(fileName).bufferedReader().use {
                    it.readText()
                }
                return json.decodeFromString<WordDatabase>(jsonString)
            } catch (e: Exception) {
                // Try next file
            }
        }
        return null
    }
}
