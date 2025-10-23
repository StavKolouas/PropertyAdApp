package com.xe.propertyad.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class Autocomplete(
    private val api: ApiService,
    private val cache: AutocompleteCache
) {
    suspend fun getSuggestions(query: String): List<Suggestion> = withContext(Dispatchers.IO) {
        cache.getSuggestions(query)?.let { return@withContext it }

        return@withContext try {
            val response = api.getSuggestions(query)
            cache.saveSuggestions(query, response)
            response
        } catch (e: HttpException) {
            Log.e("Autocomplete", "HTTP error ${e.code()}: ${e.message()}")
            emptyList()
        } catch (e: IOException) {
            Log.e("Autocomplete", "Network error: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e("Autocomplete", "Unexpected error: ${e.message}")
            emptyList()
        }
    }
}
