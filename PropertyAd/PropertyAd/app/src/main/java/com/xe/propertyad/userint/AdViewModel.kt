package com.xe.propertyad.userint

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xe.propertyad.data.Autocomplete
import com.xe.propertyad.data.AutocompleteCache
import com.xe.propertyad.data.NetworkModule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.xe.propertyad.data.Suggestion

data class PropertyAd(
    val title: String,
    val location: String,
    val description: String? = null,
    val price: String? = null
)

class AdViewModel(context: Context) : ViewModel() {

    private val api = NetworkModule.provideApiService()
    private val repo = Autocomplete(api, AutocompleteCache(context))

    private var debounceJob: Job? = null

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions

    private val _selectedLocationValid = MutableStateFlow(false)
    val selectedLocationValid: StateFlow<Boolean> = _selectedLocationValid

    fun onLocationChanged(query: String) {
        if (query.length < 3) {
            _suggestions.value = emptyList()
            _selectedLocationValid.value = false
            return
        }

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300) // debounce
            try {
                val results = repo.getSuggestions(query) // network + cache
                _suggestions.value = results
                _selectedLocationValid.value =
                    results.any { it.mainText.equals(query, ignoreCase = true) }
            } catch (e: retrofit2.HttpException) {
                // Handle server-side errors (e.g., 403)
                _suggestions.value = emptyList()
                _selectedLocationValid.value = false
                e.printStackTrace()
            } catch (e: java.io.IOException) {
                //internet stuff
                _suggestions.value = emptyList()
                _selectedLocationValid.value = false
                e.printStackTrace()
            } catch (e: Exception) {
                // akyra errors
                _suggestions.value = emptyList()
                _selectedLocationValid.value = false
                e.printStackTrace()
            }
        }
    }


    fun validateLocation(location: String) {
        _selectedLocationValid.value = _suggestions.value.any { it.mainText.equals(location, ignoreCase = true) }
    }
    fun buildJson(ad: PropertyAd): String {
        return Gson().toJson(ad)
    }


}
