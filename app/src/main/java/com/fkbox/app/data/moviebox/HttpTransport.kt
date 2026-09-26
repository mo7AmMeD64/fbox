package com.fkbox.app.data.moviebox

import java.io.IOException

class HttpResult(val status: Int, val xUser: String?, val body: String)

/** Blocking HTTP abstraction so the MovieBox logic can run (and be tested) without Android. */
interface HttpTransport {
    /** Must throw IOException on network failure. [body] is sent byte-for-byte as given. */
    @Throws(IOException::class)
    fun execute(method: String, url: String, headers: Map<String, String>, body: ByteArray?): HttpResult
}

interface TokenStore {
    fun load(): String?
    fun save(token: String?)
}

class MemoryTokenStore : TokenStore {
    @Volatile private var token: String? = null
    override fun load() = token
    override fun save(token: String?) { this.token = token }
}
