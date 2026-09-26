package com.fkbox.app.player

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fkbox.app.data.moviebox.LinkType
import com.fkbox.app.data.moviebox.StreamLink
import com.fkbox.app.fkApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.fkApp

    var links by mutableStateOf<List<StreamLink>>(emptyList())
        private set
    var index by mutableIntStateOf(0)
    var loading by mutableStateOf(true)
        private set
    /** null = fine; NO_SOURCES = the server returned nothing playable; otherwise an error message. */
    var error by mutableStateOf<String?>(null)
        private set
    /** Bumped every time a new episode's sources arrive (lets the player restart from 0). */
    var generation by mutableIntStateOf(0)
        private set

    private var job: Job? = null

    fun load(id: String, season: Int, episode: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            loading = true
            error = null
            try {
                val result = withContext(Dispatchers.IO) { ctx.client.links(id, season, episode) }
                // mpv can play direct/HLS/DASH links; magnet and torrent links are not playable here.
                val playable = result.links.filter { it.type != LinkType.MAGNET && it.type != LinkType.TORRENT }
                if (playable.isEmpty()) {
                    links = emptyList()
                    error = NO_SOURCES
                } else {
                    links = playable
                    index = playable.indexOfFirst { it.language == "Original" }.takeIf { it >= 0 } ?: 0
                    generation++
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                links = emptyList()
                error = e.message ?: e.javaClass.simpleName
            } finally {
                loading = false
            }
        }
    }

    companion object {
        const val NO_SOURCES = "no_sources"
    }
}
