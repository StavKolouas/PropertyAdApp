package com.xe.propertyad.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AutocompleteCache(context: Context) {
    private val prefs = context.getSharedPreferences("autocomplete_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSuggestions(query: String, suggestions: List<Suggestion>) {
        val json = gson.toJson(suggestions)
        prefs.edit().putString(query.lowercase(), json).apply()
    }

    fun getSuggestions(query: String): List<Suggestion>? {
        val json = prefs.getString(query.lowercase(), null) ?: return null
        val type = object : TypeToken<List<Suggestion>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
