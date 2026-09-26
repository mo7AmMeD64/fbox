package com.fkbox.app.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fkbox.app.data.moviebox.HomeRequests
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.fkApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Section(
    val loading: Boolean = true,
    val items: List<Item> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.fkApp

    val genres = HomeRequests.genres

    /** null = nothing picked yet (shows the Trending lists). */
    var selectedGenre by mutableStateOf<HomeRequests.Genre?>(null)
        private set
    var movies by mutableStateOf(Section())
        private set
    var series by mutableStateOf(Section())
        private set

    val featured: Item? get() = movies.items.firstOrNull() ?: series.items.firstOrNull()

    private var moviesJob: Job? = null
    private var seriesJob: Job? = null

    init {
        load()
        // Reload when the adult filter or the server changes in Settings.
        viewModelScope.launch { ctx.prefs.adult.drop(1).collect { load() } }
        viewModelScope.launch { ctx.prefs.host.drop(1).collect { load() } }
    }

    fun selectGenre(genre: HomeRequests.Genre) {
        selectedGenre = genre
        load()
    }

    fun load() {
        val g = selectedGenre ?: genres.first()
        moviesJob?.cancel()
        seriesJob?.cancel()
        movies = Section()
        series = Section()
        moviesJob = viewModelScope.launch { movies = fetch(g.movies) }
        seriesJob = viewModelScope.launch { series = fetch(g.series) }
    }

    private suspend fun fetch(request: String): Section = try {
        Section(loading = false, items = withContext(Dispatchers.IO) { ctx.client.mainPage(request, 1) })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Section(loading = false, error = e.message ?: e.javaClass.simpleName)
    }
}
