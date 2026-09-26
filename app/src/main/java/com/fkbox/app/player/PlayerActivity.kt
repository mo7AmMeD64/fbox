package com.fkbox.app.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fkbox.app.fkApp
import com.fkbox.app.ui.details.PlayRequest
import com.fkbox.app.ui.theme.FkboxTheme
import `is`.xyz.mpv.MPVLib
import java.io.File

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_ID)
        if (id.isNullOrEmpty()) {
            finish()
            return
        }
        val req = PlayRequest(
            id = id,
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
            season = intent.getIntExtra(EXTRA_SEASON, 0),
            episode = intent.getIntExtra(EXTRA_EPISODE, 0),
            maxEpisode = intent.getIntExtra(EXTRA_MAX_EPISODE, 0),
        )

        copyMpvAssets()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        val prefs = fkApp.prefs
        setContent {
            FkboxTheme {
                PlayerScreen(req = req, prefs = prefs, onExit = { finish() })
            }
        }
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onPause() {
        super.onPause()
        if (MpvGuard.alive) MPVLib.setPropertyBoolean("pause", true)
    }

    /** libmpv needs its CA bundle and a fallback subtitle font inside its config dir (= filesDir). */
    private fun copyMpvAssets() {
        for (name in listOf("subfont.ttf", "cacert.pem")) {
            val out = File(filesDir, name)
            if (out.exists() && out.length() > 0) continue
            try {
                assets.open(name).use { input -> out.outputStream().use { input.copyTo(it) } }
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        private const val EXTRA_ID = "id"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_SEASON = "season"
        private const val EXTRA_EPISODE = "episode"
        private const val EXTRA_MAX_EPISODE = "max_episode"

        fun intent(context: Context, req: PlayRequest): Intent =
            Intent(context, PlayerActivity::class.java)
                .putExtra(EXTRA_ID, req.id)
                .putExtra(EXTRA_TITLE, req.title)
                .putExtra(EXTRA_SEASON, req.season)
                .putExtra(EXTRA_EPISODE, req.episode)
                .putExtra(EXTRA_MAX_EPISODE, req.maxEpisode)
    }
}
