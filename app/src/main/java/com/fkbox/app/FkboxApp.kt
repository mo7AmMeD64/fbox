package com.fkbox.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.fkbox.app.data.AppPrefs
import com.fkbox.app.data.OkHttpTransport
import com.fkbox.app.data.moviebox.MovieBoxClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class FkboxApp : Application() {
    lateinit var prefs: AppPrefs
        private set
    lateinit var client: MovieBoxClient
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        prefs = AppPrefs(this)
        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        client = MovieBoxClient(
            http = OkHttpTransport(http),
            tokens = prefs,
            host = prefs.host.value,
            enableAdult = prefs.adult.value,
            log = { if (BuildConfig.DEBUG) Log.d("Fkbox", it) },
        )
        // Keep the client in sync with settings changes.
        scope.launch { prefs.adult.collect { client.enableAdult = it } }
        scope.launch { prefs.host.collect { client.host = it } }
    }
}

val Context.fkApp: FkboxApp get() = applicationContext as FkboxApp
