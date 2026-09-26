package com.fkbox.app.ui.details

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fkbox.app.data.moviebox.Details
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.fkApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DetailsUi {
    data object Loading : DetailsUi
    data class Error(val message: String) : DetailsUi
    data class Ready(val details: Details) : DetailsUi
}

class DetailsViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val ctx = app.fkApp
    val id: String = handle.get<String>("id").orEmpty()

    var ui by mutableStateOf<DetailsUi>(DetailsUi.Loading)
        private set
    var recommendations by mutableStateOf<List<Item>>(emptyList())
        private set
    var selectedSeason by mutableIntStateOf(1)

    val isSaved: StateFlow<Boolean> = ctx.prefs.favorites
        .map { list -> list.any { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ctx.prefs.isFavorite(id))

    init { load() }

    fun load() {
        ui = DetailsUi.Loading
        viewModelScope.launch {
            try {
                val d = withContext(Dispatchers.IO) { ctx.client.details(id) }
                selectedSeason = d.seasons.firstOrNull()?.season ?: 1
                ui = DetailsUi.Ready(d)
                // Recommendations are optional; never block or fail the page on them.
                try {
                    recommendations = withContext(Dispatchers.IO) { ctx.client.recommendations(id) }
                        .filter { it.id != id }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ui = DetailsUi.Error(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    /** Toggles saved state and returns true when the title is now saved. */
    fun toggleSaved(): Boolean {
        val d = (ui as? DetailsUi.Ready)?.details ?: return isSaved.value
        return ctx.prefs.toggleFavorite(
            Item(id = d.id, title = d.title, posterUrl = d.posterUrl, isSeries = d.isSeries, imdb = d.imdb)
        )
    }

    fun savedItem(): Item? = (ui as? DetailsUi.Ready)?.details?.let {
        Item(it.id, it.title, it.posterUrl, it.isSeries, it.imdb)
    }
}
