package com.fkbox.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fkbox.app.player.PlayerActivity
import com.fkbox.app.ui.nav.FkboxRoot
import com.fkbox.app.ui.theme.FkboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            FkboxTheme {
                FkboxRoot(onPlay = { req -> startActivity(PlayerActivity.intent(this, req)) })
            }
        }
    }
}
