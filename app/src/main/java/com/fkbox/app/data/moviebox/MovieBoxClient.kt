package com.fkbox.app.data.moviebox

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

const val API_PREFIX = "/wefeed-mobile-bff"
const val TOKEN_HOST = "https://apig.inmoviebox.com"

val HOST_POOL = listOf(
    "https://api6.aoneroom.com",
    "https://api5.aoneroom.com",
    "https://api4.aoneroom.com",
    "https://api4sg.aoneroom.com",
    "https://api3.aoneroom.com",
)

private const val USER_AGENT =
    "com.community.mbox.in/50020126 (Linux; U; Android 14; en_IN; Pixel 8; Build/UD1A.230803.041; Cronet/145.0.7582.0)"
private const val TOKEN_QUERY_CATEGORY = "4516404531735022304"
private const val BLOCKED_URL_MARKER = "b164fbfb4347792950bdfbfb563d39d9"
private const val BLOCKED_PATH_MARKER = "/other/2026/09/04/"
private val QUALITIES = intArrayOf(2160, 1440, 1080, 720, 480, 360, 240)

// ---------- small JSON helpers (behave the same on Android's org.json and the reference org.json) ----------

internal fun JSONObject.str(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val v = opt(key)?.toString() ?: return null
    return if (v.isBlank() || v == "null") null else v
}

internal fun JSONObject.obj(key: String): JSONObject? = optJSONObject(key)
internal fun JSONObject.arr(key: String): JSONArray? = optJSONArray(key)
internal fun JSONArray?.objects(): List<JSONObject> {
    if (this == null) return emptyList()
    val out = ArrayList<JSONObject>(length())
    for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
    return out
}

private fun toIntOr(v: Any?, default: Int): Int = v?.toString()?.trim()?.toIntOrNull() ?: default
private fun toDoubleOrNull(v: String?): Double? = v?.trim()?.toDoubleOrNull()

private fun encode(v: Any): String = URLEncoder.encode(v.toString(), "UTF-8").replace("+", "%20")

/** Compact JSON object with a fixed key order (what the plugin signs is exactly what we send). */
private fun jsonBody(vararg pairs: Pair<String, Any>): String =
    pairs.joinToString(",", "{", "}") { (k, v) ->
        val value = if (v is String) JSONObject.quote(v) else v.toString()
        "${JSONObject.quote(k)}:$value"
    }

// ---------- content filter (port of isNsfwItem) ----------

private val ADULT_GENRES = setOf("adult", "erotic", "erotica", "hot")
private val ADULT_KEYWORDS = listOf(
    "porn", "hentai", "xxx", "seduced", "nude", "naked", "erotica", "vivamax", "ullu",
    "charmsukh", "kooku", "primeplay", "bhabhi", "bhabhiji", "x-rated", "سكس", "جنس",
)
private val PROVOCATIVE = listOf("pleasure", "seductive", "naked", "escort", "affair", "lover", "mistress", "sensual")
private val SEX_WORD = Regex("\\bsex\\b")

fun isNsfwItem(item: JSONObject, enableAdult: Boolean): Boolean {
    if (enableAdult) return false
    val genre = (item.str("genre") ?: "").lowercase()
    val tokens = genre.split(",").map { it.trim() }
    val title = (item.str("title") ?: "").lowercase()
    val subjectType = toIntOr(item.str("subjectType"), 1)
    val restrictKid = toIntOr(item.str("restrictKid"), 0)
    val rating = (item.str("contentRating") ?: "").uppercase()

    if (tokens.any { it in ADULT_GENRES } || SEX_WORD.containsMatchIn(title)) return true
    if (ADULT_KEYWORDS.any { title.contains(it) }) return true
    val mature = rating == "R" || rating == "TV-MA"
    if (restrictKid == 1 && mature && tokens.any { it in setOf("romance", "erotic", "hot", "drama") }) return true
    if (subjectType != 7) return false
    if (!genre.contains("romance") && !genre.contains("drama")) return false
    return PROVOCATIVE.any { title.contains(it) }
}

// ---------- link helpers ----------

fun highestQuality(text: String?): Int? {
    if (text.isNullOrEmpty()) return null
    return QUALITIES.firstOrNull { text.contains(it.toString()) }
}

fun detectLinkType(url: String, format: String): LinkType {
    val low = url.lowercase()
    return when {
        low.contains(".mpd") -> LinkType.DASH
        low.startsWith("magnet:") -> LinkType.MAGNET
        url.substringAfterLast('.', "").lowercase() == "torrent" -> LinkType.TORRENT
        format.equals("HLS", ignoreCase = true) || low.contains(".m3u8") -> LinkType.M3U8
        low.contains(".mp4") || low.contains(".mkv") -> LinkType.VIDEO
        else -> LinkType.INFER
    }
}

/** Extracts the real .mpd resource from a CloudFront-Policy cookie (extractPolicyResource in the plugin). */
fun extractPolicyResource(signCookie: String?): String? {
    if (signCookie.isNullOrBlank()) return null
    val raw = Regex("CloudFront-Policy=([^;]+)").find(signCookie)?.groupValues?.get(1) ?: return null
    val variants = listOf(
        raw.replace('-', '+').replace('~', '/').replace('_', '='),
        raw.replace('-', '+').replace('_', '/'),
    )
    for (v in variants) {
        try {
            val padded = v + "=".repeat((4 - v.length % 4) % 4)
            val json = String(java.util.Base64.getDecoder().decode(padded), Charsets.UTF_8)
            val resource = JSONObject(json).getJSONArray("Statement").getJSONObject(0).getString("Resource")
            val trimmed = resource.trimEnd('*', '/')
            return if (trimmed.lowercase().endsWith(".mpd")) trimmed else "$trimmed/index.mpd"
        } catch (_: Exception) {
        }
    }
    return null
}

/** "id|season|episode" -> Triple(id, season, episode) */
fun parseLinkData(data: String): Triple<String, Int, Int> {
    val parts = data.split("|")
    val raw = parts[0]
    val sid = Regex("subjectId=([^&]+)").find(raw)?.groupValues?.get(1) ?: raw.substringAfterLast('/')
    val season = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    val episode = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
    return Triple(sid, season, episode)
}

class MovieBoxClient(
    private val http: HttpTransport,
    private val tokens: TokenStore = MemoryTokenStore(),
    host: String = HOST_POOL[4],
    @Volatile var enableAdult: Boolean = false,
    private val tokenHost: String = TOKEN_HOST,
    private val hostPool: List<String> = HOST_POOL,
    private val executor: ExecutorService = Executors.newFixedThreadPool(6),
    private val log: (String) -> Unit = {},
) {
    @Volatile var host: String = host.trimEnd('/')
    @Volatile private var bearer: String? = tokens.load()?.takeIf { MovieBoxCrypto.isTokenValid(it) }

    private val restrictKid get() = if (enableAdult) 0 else 1
    private val deviceId = Random.nextBytes(16).joinToString("") { "%02x".format(it) }
    private val clientInfo: String = listOf(
        "package_name" to "com.community.mbox.in",
        "version_name" to "4.0.02.0831.03",
        "version_code" to 50020126,
        "os" to "android",
        "os_version" to "14",
        "device_id" to deviceId,
        "install_store" to "official",
        "gaid" to "1b2212c1-dadf-43c3-a0c8-bd6ce48ae22d",
        "brand" to "Google",
        "model" to "Pixel 8",
        "system_language" to "en",
        "net" to "NETWORK_WIFI",
        "region" to "IN",
        "timezone" to "Asia/Calcutta",
        "sp_code" to "",
    ).joinToString(",", "{", "}") { (k, v) ->
        "\"$k\":" + if (v is String) JSONObject.quote(v) else v.toString()
    }

    // ------------------------------------------------------------------ auth

    private fun saveToken(token: String) {
        if (!MovieBoxCrypto.isTokenValid(token)) return
        bearer = token
        try { tokens.save(token) } catch (_: Exception) {}
    }

    private fun clearToken() {
        bearer = null
        try { tokens.save(null) } catch (_: Exception) {}
    }

    private fun persistTokenFromXUser(xUser: String?) {
        if (xUser.isNullOrBlank()) return
        try {
            JSONObject(xUser).str("token")?.let { saveToken(it) }
        } catch (_: Exception) {}
    }

    private fun makeHeaders(method: String, url: String, body: String?, token: String?): Map<String, String> {
        val accept = "application/json"
        val contentType = if (body != null) "application/json; charset=utf-8" else "application/json"
        val ts = System.currentTimeMillis()
        val h = LinkedHashMap<String, String>()
        h["user-agent"] = USER_AGENT
        h["accept"] = accept
        h["content-type"] = contentType
        h["x-client-token"] = MovieBoxCrypto.clientToken(ts)
        h["x-tr-signature"] = MovieBoxCrypto.signature(method, accept, contentType, url, body, false, ts)
        h["x-client-info"] = clientInfo
        h["x-client-status"] = "0"
        if (!token.isNullOrEmpty()) h["Authorization"] = "Bearer $token"
        return h
    }

    /** Fetches an anonymous token from the x-user response header (fetchAnonymousToken in the plugin). */
    @Synchronized
    fun fetchAnonymousToken(): String {
        for (h in listOf(tokenHost) + hostPool) {
            val url = "$h$API_PREFIX/tab/ranking-list?tabId=0&categoryType=$TOKEN_QUERY_CATEGORY&page=1&perPage=1"
            try {
                val r = http.execute("GET", url, makeHeaders("GET", url, null, null), null)
                log("token fetch $h -> ${r.status}")
                persistTokenFromXUser(r.xUser)
                if (MovieBoxCrypto.isTokenValid(bearer)) return bearer!!
            } catch (e: IOException) {
                log("token fetch failed on $h: $e")
            }
        }
        return bearer ?: ""
    }

    @Synchronized
    fun ensureToken(): String {
        if (MovieBoxCrypto.isTokenValid(bearer)) return bearer!!
        return fetchAnonymousToken()
    }

    fun resetToken() = clearToken()

    // ------------------------------------------------------------------ request core

    private fun request(
        method: String,
        path: String,
        query: List<Pair<String, Any>> = emptyList(),
        body: String? = null,
        retry: Boolean = true,
    ): JSONObject {
        val qs = query.joinToString("&") { (k, v) -> "$k=${encode(v)}" }
        val pathQ = if (qs.isEmpty()) path else "$path?$qs"
        val hosts = listOf(host) + hostPool.filter { it != host }
        var last: Exception? = null
        for (h in hosts) {
            val url = h + pathQ
            val token = ensureToken()
            val headers = makeHeaders(method, url, body, token)
            log("$method $url ${body ?: ""}")
            val r = try {
                http.execute(method, url, headers, body?.toByteArray(Charsets.UTF_8))
            } catch (e: IOException) {
                last = e
                log("network error on $h: $e")
                continue
            }
            persistTokenFromXUser(r.xUser)
            if (r.status == 401 || r.status == 441) {
                if (retry) {
                    log("status ${r.status} -> refreshing token")
                    clearToken()
                    return request(method, path, query, body, retry = false)
                }
                throw ApiException("${r.status} unauthorized")
            }
            if (r.status >= 500) {
                last = ApiException("${r.status} from $h")
                continue
            }
            if (r.status >= 400) throw ApiException("${r.status} from $h: ${r.body.take(200)}")
            host = h
            return try {
                JSONObject(r.body)
            } catch (e: Exception) {
                throw ApiException("Unexpected response from $h", e)
            }
        }
        throw (last as? ApiException) ?: ApiException("Network error: all servers failed", last)
    }

    private fun <T> parallel(tasks: List<() -> T>): List<Result<T>> {
        val futures = executor.invokeAll(tasks.map { t -> Callable { t() } })
        return futures.map { f ->
            try { Result.success(f.get()) } catch (e: Exception) { Result.failure(e.cause ?: e) }
        }
    }

    // ------------------------------------------------------------------ lists

    private fun parseItems(items: JSONArray?, cutBracket: Boolean): List<Item> {
        val out = ArrayList<Item>()
        val seen = HashSet<String>()
        for (it in items.objects()) {
            if (isNsfwItem(it, enableAdult)) continue
            var title = it.str("title") ?: continue
            val id = it.str("subjectId") ?: continue
            if (!seen.add(id)) continue
            if (cutBracket) title = title.substringBefore("[").trim()
            out.add(
                Item(
                    id = id,
                    title = title,
                    posterUrl = it.obj("cover")?.str("url"),
                    isSeries = toIntOr(it.str("subjectType"), 1).let { t -> t == 2 || t == 7 },
                    imdb = toDoubleOrNull(it.str("imdbRatingValue")),
                )
            )
        }
        return out
    }

    /** [requestData] uses the plugin's format: "r|tab|categoryType" (ranking) or "1|channel;key=value;..." (list). */
    fun mainPage(requestData: String, page: Int = 1): List<Item> {
        val resp: JSONObject
        if (requestData.startsWith("r|")) {
            val parts = requestData.split("|")
            if (parts.size < 3) return emptyList()
            resp = request(
                "GET", "$API_PREFIX/tab/ranking-list",
                query = listOf(
                    "tabId" to (parts[1].ifEmpty { "0" }), "categoryType" to parts[2],
                    "page" to page, "perPage" to 20, "restrictKid" to restrictKid,
                ),
            )
        } else {
            val head = requestData.substringBefore(";")
            val rest = if (requestData.contains(";")) requestData.substringAfter(";") else ""
            val channel = head.split("|").getOrNull(1) ?: ""
            val filters = HashMap<String, String>()
            if (rest.isNotEmpty()) for (f in rest.split(";")) {
                val k = f.substringBefore("=").trim()
                val v = f.substringAfter("=", "").trim()
                if (k.isNotEmpty() && v.isNotEmpty()) filters[k] = v
            }
            val body = jsonBody(
                "page" to page, "perPage" to 20, "channelId" to channel,
                "classify" to (filters["classify"] ?: "All"),
                "country" to (filters["country"] ?: "All"),
                "year" to (filters["year"] ?: "All"),
                "genre" to (filters["genre"] ?: "All"),
                "sort" to (filters["sort"] ?: "ForYou"),
                "restrictKid" to restrictKid,
            )
            resp = request("POST", "$API_PREFIX/subject-api/list", body = body)
        }
        val data = resp.obj("data")
        val items = data?.arr("items") ?: data?.arr("subjects")
        return parseItems(items, cutBracket = true)
    }

    fun search(query: String, page: Int = 1): List<Item> {
        val body = jsonBody("page" to page, "perPage" to 20, "keyword" to query, "restrictKid" to restrictKid)
        val resp = request("POST", "$API_PREFIX/subject-api/search/v2", body = body)
        val out = ArrayList<Item>()
        val seen = HashSet<String>()
        for (group in resp.obj("data")?.arr("results").objects()) {
            for (item in parseItems(group.arr("subjects"), cutBracket = false)) {
                if (seen.add(item.id)) out.add(item)
            }
        }
        return out
    }

    fun recommendations(subjectId: String): List<Item> {
        val body = jsonBody("subjectId" to subjectId, "page" to 1, "perPage" to 20)
        val resp = request("POST", "$API_PREFIX/subject-api/detail-rec", body = body)
        return parseItems(resp.obj("data")?.arr("items"), cutBracket = true)
    }

    // ------------------------------------------------------------------ details

    private fun parseDubs(data: JSONObject): List<Dub> =
        data.arr("dubs").objects().mapNotNull {
            val id = it.str("subjectId")
            val lan = it.str("lanName")
            if (id != null && lan != null) Dub(id, lan) else null
        }

    fun details(subject: String): Details {
        val sid = parseLinkData(subject).first
        val resp = request("GET", "$API_PREFIX/subject-api/get", listOf("subjectId" to sid))
        val data = resp.obj("data")
        if (data == null || data.length() == 0) throw ApiException("No data")
        if (isNsfwItem(data, enableAdult)) throw ApiException("Blocked by the adult-content filter. Enable it in Settings to view this title.")
        val rawTitle = data.str("title") ?: throw ApiException("No title in response")

        val release = data.str("releaseDate")
        val isSeries = toIntOr(data.str("subjectType"), 1).let { it == 2 || it == 7 }
        val dubs = parseDubs(data)
        val actors = data.arr("staffList").objects().mapNotNull {
            val name = it.str("name") ?: return@mapNotNull null
            Actor(name, it.str("character"), it.str("avatarUrl"))
        }

        var seasons = emptyList<SeasonInfo>()
        if (isSeries) {
            val ids = (listOf(sid) + dubs.map { it.subjectId }).distinct()
            val replies = parallel(ids.map { id ->
                { request("GET", "$API_PREFIX/subject-api/season-info", listOf("subjectId" to id)) }
            })
            val merged = java.util.TreeMap<Int, Int>()
            for (r in replies) {
                val resp2 = r.getOrNull() ?: continue
                for (s in resp2.obj("data")?.arr("seasons").objects()) {
                    val se = toIntOr(s.str("se"), 1)
                    val mx = toIntOr(s.str("maxEp"), 1)
                    merged[se] = maxOf(merged[se] ?: 0, mx)
                }
            }
            seasons = merged.map { SeasonInfo(it.key, it.value) }
        }

        return Details(
            id = sid,
            title = rawTitle.substringBefore("[").trim(),
            isSeries = isSeries,
            description = data.str("description"),
            posterUrl = data.obj("cover")?.str("url"),
            year = release?.take(4)?.toIntOrNull(),
            duration = data.str("duration"),
            genres = (data.str("genre") ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() },
            country = data.str("countryName"),
            language = data.str("language"),
            imdb = toDoubleOrNull(data.str("imdbRatingValue")),
            actors = actors,
            dubs = dubs,
            seasons = seasons,
        )
    }

    // ------------------------------------------------------------------ links

    /**
     * [subject] = subjectId or "id|season|episode". When season/episode are null: series -> S1E1, movie -> 0/0.
     */
    fun links(subject: String, season: Int? = null, episode: Int? = null, withSubtitles: Boolean = true): LinksResult {
        var s = season
        var e = episode
        val sid: String
        if (subject.contains("|")) {
            val (id, s2, e2) = parseLinkData(subject)
            sid = id; s = s2; e = e2
        } else {
            sid = parseLinkData(subject).first
        }

        val pairs = ArrayList<Pair<String, String>>()
        pairs.add(sid to "Original")
        try {
            val d = request("GET", "$API_PREFIX/subject-api/get", listOf("subjectId" to sid)).obj("data")
            if (d != null) {
                if (s == null && e == null && toIntOr(d.str("subjectType"), 1).let { it == 2 || it == 7 }) {
                    s = 1; e = 1
                }
                for (dub in parseDubs(d)) if (pairs.none { it.first == dub.subjectId }) pairs.add(dub.subjectId to dub.language)
            }
        } catch (ex: ApiException) {
            log("dubs lookup failed: $ex")
        }
        val fs = s ?: 0
        val fe = e ?: 0

        val results = parallel(pairs.map { (id, lang) -> { linksFor(id, lang, fs, fe, withSubtitles) } })
        val links = ArrayList<StreamLink>()
        val report = LinkedHashMap<String, String>()
        val seen = HashSet<String>()
        pairs.forEachIndexed { i, (id, lang) ->
            val key = "$lang [$id]"
            val r = results[i]
            if (r.isFailure) {
                report[key] = "error: ${r.exceptionOrNull()?.message}"
            } else {
                val list = r.getOrThrow()
                report[key] = if (list.isEmpty()) "no links returned" else "ok"
                for (l in list) if (seen.add(l.url)) links.add(l)
            }
        }
        links.sortByDescending { it.quality ?: 0 }
        return LinksResult(links, report)
    }

    private fun linkHeaders(cookie: String?): Map<String, String> {
        val h = LinkedHashMap<String, String>()
        h["Referer"] = "$host/"
        h["User-Agent"] = USER_AGENT
        if (!cookie.isNullOrEmpty()) h["Cookie"] = cookie
        return h
    }

    private fun linksFor(sid: String, lang: String, season: Int, episode: Int, withSubtitles: Boolean): List<StreamLink> {
        val resp = request(
            "GET", "$API_PREFIX/subject-api/play-info",
            listOf("subjectId" to sid, "se" to season, "ep" to episode),
        )
        val streams = resp.obj("data")?.arr("streams")
        val label = lang.replace("dub", "Audio")
        val links = ArrayList<StreamLink>()
        val streamIds = ArrayList<String>()

        if (streams != null && streams.length() > 0) {
            for (st in streams.objects()) {
                val rawUrl = st.str("url") ?: continue
                val fmt = st.str("format") ?: ""
                val cookie = st.str("signCookie")
                val quality = highestQuality(st.str("resolutions"))
                val finalUrl = extractPolicyResource(cookie) ?: rawUrl
                if (finalUrl.contains(BLOCKED_URL_MARKER)) continue
                if (finalUrl == rawUrl && rawUrl.contains(BLOCKED_PATH_MARKER)) continue
                links.add(
                    StreamLink(
                        url = finalUrl,
                        name = "MovieBox $label" + (quality?.let { " ${it}p" } ?: ""),
                        quality = quality,
                        type = detectLinkType(finalUrl, fmt),
                        headers = linkHeaders(cookie),
                        subtitles = emptyList(),
                        language = lang,
                    )
                )
                st.str("id")?.let { streamIds.add(it) }
            }
            if (withSubtitles && streamIds.isNotEmpty()) {
                val subs = captions(sid, streamIds, lang)
                return links.map { it.copy(subtitles = subs) }
            }
            return links
        }

        // Fallback: direct resources (resourceDetectors)
        val d2 = request("GET", "$API_PREFIX/subject-api/get", listOf("subjectId" to sid)).obj("data")
        for (det in d2?.arr("resourceDetectors").objects()) {
            for (r in det.arr("resolutionList").objects()) {
                val link = r.str("resourceLink") ?: continue
                val res = toIntOr(r.str("resolution"), 0)
                val se = r.str("se")
                val ep = r.str("ep")
                if (!(season == 0 && episode == 0)) {
                    if (se == null || ep == null || toIntOr(se, -1) != season || toIntOr(ep, -1) != episode) continue
                }
                var nm = "MovieBox $label"
                if (se != null) nm += " S${toIntOr(se, 0)}E${toIntOr(ep, 0)}"
                nm += " ${res}p"
                links.add(StreamLink(link, nm, res.takeIf { it > 0 }, LinkType.VIDEO, linkHeaders(null), emptyList(), lang))
            }
        }
        return links
    }

    private fun captions(sid: String, streamIds: List<String>, lang: String): List<Subtitle> {
        val jobs = ArrayList<() -> List<JSONObject>>()
        for (id in streamIds.distinct()) {
            jobs.add {
                try {
                    request("GET", "$API_PREFIX/subject-api/get-stream-captions", listOf("subjectId" to sid, "streamId" to id))
                        .obj("data")?.arr("extCaptions").objects()
                } catch (e: Exception) { emptyList() }
            }
            jobs.add {
                try {
                    request("GET", "$API_PREFIX/subject-api/get-ext-captions", listOf("subjectId" to sid, "resourceId" to id, "episode" to 0))
                        .obj("data")?.arr("extCaptions").objects()
                } catch (e: Exception) { emptyList() }
            }
        }
        val subs = ArrayList<Subtitle>()
        val seen = HashSet<String>()
        for (r in parallel(jobs)) {
            for (c in r.getOrNull().orEmpty()) {
                val url = c.str("url") ?: continue
                if (!seen.add(url)) continue
                var name = c.str("lanName") ?: c.str("lan") ?: c.str("language") ?: "Unknown"
                val code = c.str("lan") ?: c.str("language")
                if (lang != "Original") name = "$name ($lang)"
                subs.add(Subtitle(url, name, code))
            }
        }
        return subs
    }

    fun shutdown() { executor.shutdown() }
}
