package com.fkbox.app.data

import android.content.Context
import com.fkbox.app.data.moviebox.HOST_POOL
import com.fkbox.app.data.moviebox.Item
import com.fkbox.app.data.moviebox.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Settings + saved titles + session token, persisted in SharedPreferences and exposed as flows. */
class AppPrefs(context: Context) : TokenStore {
    private val sp = context.applicationContext.getSharedPreferences("fkbox", Context.MODE_PRIVATE)

    private val _adult = MutableStateFlow(sp.getBoolean(K_ADULT, false))
    val adult: StateFlow<Boolean> = _adult.asStateFlow()

    private val _host = MutableStateFlow(sp.getString(K_HOST, null)?.takeIf { it in HOST_POOL } ?: HOST_POOL[4])
    val host: StateFlow<String> = _host.asStateFlow()

    private val _hwdec = MutableStateFlow(sp.getBoolean(K_HWDEC, true))
    val hwdec: StateFlow<Boolean> = _hwdec.asStateFlow()

    private val _strictTls = MutableStateFlow(sp.getBoolean(K_TLS, true))
    val strictTls: StateFlow<Boolean> = _strictTls.asStateFlow()

    private val _subLangs = MutableStateFlow(sp.getString(K_SUBLANG, DEFAULT_SUB_LANGS) ?: DEFAULT_SUB_LANGS)
    val subLangs: StateFlow<String> = _subLangs.asStateFlow()

    private val _favorites = MutableStateFlow(readFavorites())
    val favorites: StateFlow<List<Item>> = _favorites.asStateFlow()

    fun setAdult(v: Boolean) { sp.edit().putBoolean(K_ADULT, v).apply(); _adult.value = v }
    fun setHost(v: String) { sp.edit().putString(K_HOST, v).apply(); _host.value = v }
    fun setHwdec(v: Boolean) { sp.edit().putBoolean(K_HWDEC, v).apply(); _hwdec.value = v }
    fun setStrictTls(v: Boolean) { sp.edit().putBoolean(K_TLS, v).apply(); _strictTls.value = v }
    fun setSubLangs(v: String) { sp.edit().putString(K_SUBLANG, v).apply(); _subLangs.value = v }

    // ---- saved titles ----

    fun isFavorite(id: String) = _favorites.value.any { it.id == id }

    /** Adds or removes the item; returns true when it is now saved. */
    fun toggleFavorite(item: Item): Boolean {
        val list = _favorites.value
        val exists = list.any { it.id == item.id }
        val updated = if (exists) list.filterNot { it.id == item.id } else listOf(item) + list
        writeFavorites(updated)
        return !exists
    }

    fun addFavorite(item: Item) {
        if (!isFavorite(item.id)) writeFavorites(listOf(item) + _favorites.value)
    }

    fun clearFavorites() = writeFavorites(emptyList())

    private fun writeFavorites(list: List<Item>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id).put("title", it.title)
                    .put("poster", it.posterUrl ?: JSONObject.NULL)
                    .put("series", it.isSeries)
                    .put("imdb", it.imdb ?: JSONObject.NULL)
            )
        }
        sp.edit().putString(K_FAVS, arr.toString()).apply()
        _favorites.value = list
    }

    private fun readFavorites(): List<Item> = try {
        val arr = JSONArray(sp.getString(K_FAVS, "[]"))
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val id = o.optString("id", "")
            if (id.isEmpty()) return@mapNotNull null
            Item(
                id = id,
                title = o.optString("title", ""),
                posterUrl = if (o.isNull("poster")) null else o.optString("poster"),
                isSeries = o.optBoolean("series", false),
                imdb = if (o.isNull("imdb")) null else o.optDouble("imdb"),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    // ---- session token (TokenStore) ----

    override fun load(): String? = sp.getString(K_TOKEN, null)

    override fun save(token: String?) {
        val editor = sp.edit()
        if (token == null) editor.remove(K_TOKEN) else editor.putString(K_TOKEN, token)
        editor.apply()
    }

    companion object {
        const val DEFAULT_SUB_LANGS = "arabic,ar"
        private const val K_ADULT = "adult"
        private const val K_HOST = "host"
        private const val K_HWDEC = "hwdec"
        private const val K_TLS = "strict_tls"
        private const val K_SUBLANG = "sub_langs"
        private const val K_FAVS = "favorites"
        private const val K_TOKEN = "token"
    }
}
