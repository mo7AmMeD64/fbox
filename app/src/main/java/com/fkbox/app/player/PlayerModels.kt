package com.fkbox.app.player

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fkbox.app.data.moviebox.StreamLink
import com.fkbox.app.data.moviebox.Subtitle
import `is`.xyz.mpv.MPVLib
import java.util.Locale

data class Track(
    val id: Int,
    val type: String,
    val title: String?,
    val lang: String?,
    val selected: Boolean,
    val height: Int?,
)

/** Observable playback state shown by the controls. */
class PlayerState {
    var position by mutableDoubleStateOf(0.0)
    var duration by mutableDoubleStateOf(0.0)
    var paused by mutableStateOf(false)
    var buffering by mutableStateOf(true)
    var speed by mutableDoubleStateOf(1.0)
    var error by mutableStateOf<String?>(null)
    var tracks by mutableStateOf<List<Track>>(emptyList())
}

/** True while libmpv is created (guards calls made from lifecycle callbacks). */
object MpvGuard {
    @Volatile var alive = false
}

/** Receives libmpv events on the native thread and forwards them to Compose state on the main thread. */
class MpvController(
    private val state: PlayerState,
    private val preferredSubLangs: List<String>,
    private val onEnded: () -> Unit,
) : MPVLib.EventObserver {
    private val main = Handler(Looper.getMainLooper())

    /** External subtitles of the source that is currently loaded. */
    @Volatile var subtitles: List<Subtitle> = emptyList()

    fun applyLink(link: StreamLink, startSeconds: Double) {
        link.headers["User-Agent"]?.let { MPVLib.setOptionString("user-agent", it) }
        val fields = link.headers.filterKeys { it != "User-Agent" }
            .entries.joinToString(",") { "${it.key}: ${it.value}" }
        MPVLib.setOptionString("http-header-fields", fields)
        MPVLib.setOptionString("start", if (startSeconds > 1.0) String.format(Locale.US, "%.1f", startSeconds) else "0")
    }

    fun load(link: StreamLink, startSeconds: Double) {
        subtitles = link.subtitles
        applyLink(link, startSeconds)
        MPVLib.command(arrayOf("loadfile", link.url))
    }

    // ---- EventObserver ----

    override fun eventProperty(property: String) {
        if (property == "track-list") main.post { refreshTracks() }
    }

    override fun eventProperty(property: String, value: Long) {}

    override fun eventProperty(property: String, value: Boolean) {
        main.post {
            when (property) {
                "pause" -> state.paused = value
                "paused-for-cache" -> state.buffering = value
                "eof-reached" -> if (value) onEnded()
            }
        }
    }

    override fun eventProperty(property: String, value: String) {}

    override fun eventProperty(property: String, value: Double) {
        main.post {
            when (property) {
                "time-pos" -> state.position = value
                "duration/full" -> state.duration = value
                "speed" -> state.speed = value
            }
        }
    }

    override fun event(eventId: Int) {
        main.post {
            when (eventId) {
                MPVLib.mpvEventId.MPV_EVENT_START_FILE -> {
                    state.error = null
                    state.buffering = true
                }
                MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED -> {
                    addExternalSubtitles()
                    refreshTracks()
                }
                MPVLib.mpvEventId.MPV_EVENT_PLAYBACK_RESTART -> state.buffering = false
            }
        }
    }

    override fun efEvent(err: String?) {
        if (err != null) main.post { state.error = err; state.buffering = false }
    }

    // ---- tracks & subtitles ----

    fun refreshTracks() {
        if (!MpvGuard.alive) return
        val n = MPVLib.getPropertyInt("track-list/count") ?: 0
        val out = ArrayList<Track>()
        for (i in 0 until n) {
            val type = MPVLib.getPropertyString("track-list/$i/type") ?: continue
            val id = MPVLib.getPropertyInt("track-list/$i/id") ?: continue
            out.add(
                Track(
                    id = id,
                    type = type,
                    title = MPVLib.getPropertyString("track-list/$i/title"),
                    lang = MPVLib.getPropertyString("track-list/$i/lang"),
                    selected = MPVLib.getPropertyBoolean("track-list/$i/selected") ?: false,
                    height = MPVLib.getPropertyInt("track-list/$i/demux-h"),
                )
            )
        }
        state.tracks = out
    }

    private fun matchesPreferred(s: Subtitle): Boolean {
        val label = s.label.lowercase()
        val code = s.code?.lowercase()
        return preferredSubLangs.any { p ->
            code == p || label == p || label.startsWith("$p ") || (p.length > 2 && label.startsWith(p))
        }
    }

    private fun addExternalSubtitles() {
        if (!MpvGuard.alive) return
        var selected = false
        for (s in subtitles) {
            val pick = !selected && matchesPreferred(s)
            val flag = if (pick) "select" else "auto"
            val cmd = if (s.code.isNullOrBlank()) arrayOf("sub-add", s.url, flag, s.label)
            else arrayOf("sub-add", s.url, flag, s.label, s.code)
            MPVLib.command(cmd)
            if (pick) selected = true
        }
    }
}

fun formatTime(seconds: Double): String {
    val total = seconds.toLong().coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s) else String.format(Locale.US, "%02d:%02d", m, s)
}
