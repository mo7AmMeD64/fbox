package com.fkbox.app.data.moviebox

/** Card-level item used in lists (home rows, search, recommendations, saved). */
data class Item(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val isSeries: Boolean,
    val imdb: Double?,
)

data class Actor(val name: String, val character: String?, val image: String?)

data class Dub(val subjectId: String, val language: String)

data class SeasonInfo(val season: Int, val maxEpisode: Int)

data class Details(
    val id: String,
    val title: String,
    val isSeries: Boolean,
    val description: String?,
    val posterUrl: String?,
    val year: Int?,
    val duration: String?,
    val genres: List<String>,
    val country: String?,
    val language: String?,
    val imdb: Double?,
    val actors: List<Actor>,
    val dubs: List<Dub>,
    val seasons: List<SeasonInfo>,
)

data class Subtitle(val url: String, val label: String, val code: String?)

enum class LinkType { DASH, M3U8, VIDEO, MAGNET, TORRENT, INFER }

data class StreamLink(
    val url: String,
    val name: String,
    val quality: Int?,
    val type: LinkType,
    val headers: Map<String, String>,
    val subtitles: List<Subtitle>,
    val language: String,
)

data class LinksResult(val links: List<StreamLink>, val report: Map<String, String>)

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Sections shown on the home screen / used by the genre dropdown. */
object HomeRequests {
    // Same request strings as the CloudStream plugin's main page list.
    const val TRENDING_MOVIES = "r|0|9167640870324258216"
    const val TOP_SERIES = "r|5|719331337777440448"

    data class Genre(val label: String, val movies: String, val series: String)

    private fun list(channel: Int, extra: String = "") = "1|$channel$extra"

    val genres: List<Genre> = listOf(
        Genre("Trending", TRENDING_MOVIES, TOP_SERIES),
        Genre("Anime", "1|1006", "2|2;country=Japan;genre=Animation"),
        Genre("Drama", "1|1;genre=Drama", "1|2;genre=Drama"),
        Genre("Action", "1|1;genre=Action", "1|2;genre=Action"),
        Genre("Comedy", "1|1;genre=Comedy", "1|2;genre=Comedy"),
        Genre("Crime", "1|1;genre=Crime", "1|2;genre=Crime"),
        Genre("Romance", "1|1;genre=Romance", "1|2;genre=Romance"),
        Genre("Horror", "1|1;genre=Horror", "1|2;genre=Horror"),
        Genre("Thriller", "1|1;genre=Thriller", "1|2;genre=Thriller"),
        Genre("Sci-Fi", "1|1;genre=Sci-Fi", "1|2;genre=Sci-Fi"),
        Genre("Fantasy", "1|1;genre=Fantasy", "1|2;genre=Fantasy"),
        Genre("Animation", "1|1;genre=Animation", "1|2;genre=Animation"),
    )
}
