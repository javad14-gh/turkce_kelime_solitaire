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
        return try {
            val jsonString = context.assets.open("word_database.json").bufferedReader().use {
                it.readText()
            }
            json.decodeFromString<WordDatabase>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
