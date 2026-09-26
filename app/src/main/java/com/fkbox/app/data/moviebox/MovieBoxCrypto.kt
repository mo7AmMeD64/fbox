package com.fkbox.app.data.moviebox

import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Request signing, identical to the CloudStream plugin (verified against the decompiled code):
 *  - the HMAC key is base64-decoded TWICE (30 bytes)
 *  - canonical string = METHOD \n accept \n content-type \n body-length \n timestamp \n body-md5 \n path?sortedQuery
 */
object MovieBoxCrypto {
    private const val SECRET_DEFAULT_B64 = "NzZpUmwwN3MweFNOOWpxbUVXQXQ3OUVCSlp1bElRSXNWNjRGWnIyTw=="
    private const val SECRET_ALT_B64 = "WHFuMm5uTzQxL0w5Mm8xaXVYaFNMSFRiWHZZNFo1Wlo2Mm04bVNMQQ=="

    private fun doubleDecode(s: String): ByteArray {
        val once = Base64.getDecoder().decode(s)
        return Base64.getDecoder().decode(once)
    }

    val secretDefault: ByteArray by lazy { doubleDecode(SECRET_DEFAULT_B64) }
    val secretAlt: ByteArray by lazy { doubleDecode(SECRET_ALT_B64) }

    fun md5Hex(data: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(data).joinToString("") { "%02x".format(it) }

    fun clientToken(timestamp: Long = System.currentTimeMillis()): String {
        val ts = timestamp.toString()
        return "$ts,${md5Hex(ts.reversed().toByteArray(Charsets.UTF_8))}"
    }

    /** Same as URI.getPath()/getQuery() in the plugin: decode, then sort query pairs by key. */
    fun canonicalUrl(url: String): String {
        val uri = URI(url)
        val path = uri.path ?: ""
        val query = uri.query
        var canonicalQuery = ""
        if (!query.isNullOrBlank()) {
            val pairs = query.split("&").map { item ->
                val p = item.split("=")
                p[0] to (if (p.size > 1) p[1] else "")
            }.sortedBy { it.first }
            canonicalQuery = pairs.joinToString("&") { "${it.first}=${it.second}" }
        }
        return if (canonicalQuery.isEmpty()) path else "$path?$canonicalQuery"
    }

    fun canonicalString(
        method: String, accept: String?, contentType: String?, url: String, body: String?, timestamp: Long,
    ): String {
        val bodyHash: String
        val bodyLength: String
        if (body != null) {
            val bytes = body.toByteArray(Charsets.UTF_8)
            val hashed = if (bytes.size > 102400) bytes.copyOf(102400) else bytes
            bodyHash = md5Hex(hashed)
            bodyLength = bytes.size.toString()
        } else {
            bodyHash = ""
            bodyLength = ""
        }
        return listOf(
            method.uppercase(), accept ?: "", contentType ?: "", bodyLength,
            timestamp.toString(), bodyHash, canonicalUrl(url),
        ).joinToString("\n")
    }

    fun signature(
        method: String, accept: String?, contentType: String?, url: String, body: String?,
        useAltKey: Boolean = false, timestamp: Long = System.currentTimeMillis(),
    ): String {
        val canonical = canonicalString(method, accept, contentType, url, body, timestamp)
        val mac = Mac.getInstance("HmacMD5")
        mac.init(SecretKeySpec(if (useAltKey) secretAlt else secretDefault, "HmacMD5"))
        val digest = mac.doFinal(canonical.toByteArray(Charsets.UTF_8))
        return "$timestamp|2|${Base64.getEncoder().encodeToString(digest)}"
    }

    // ---- JWT helpers ----

    fun jwtExpiry(token: String): Long = try {
        val payload = token.split(".")[1].replace('-', '+').replace('_', '/')
        val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
        val json = String(Base64.getDecoder().decode(padded), Charsets.UTF_8)
        Regex("\"exp\"\\s*:\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    }

    fun isTokenValid(token: String?): Boolean =
        !token.isNullOrBlank() && jwtExpiry(token) > System.currentTimeMillis() / 1000 + 3600
}
