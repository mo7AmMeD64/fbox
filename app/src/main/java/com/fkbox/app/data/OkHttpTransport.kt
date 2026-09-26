package com.fkbox.app.data

import com.fkbox.app.data.moviebox.HttpResult
import com.fkbox.app.data.moviebox.HttpTransport
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class OkHttpTransport(private val client: OkHttpClient) : HttpTransport {
    @Throws(IOException::class)
    override fun execute(method: String, url: String, headers: Map<String, String>, body: ByteArray?): HttpResult {
        val builder = try {
            Request.Builder().url(url)
        } catch (e: IllegalArgumentException) {
            throw IOException("Bad URL: $url", e)
        }
        var contentType: String? = null
        for ((k, v) in headers) {
            if (k.equals("content-type", ignoreCase = true)) contentType = v else builder.header(k, v)
        }
        if (body != null) {
            // Body bytes are sent exactly as signed; the media type header comes from the body.
            builder.method(method, body.toRequestBody(contentType?.toMediaTypeOrNull()))
        } else {
            contentType?.let { builder.header("content-type", it) }
            builder.method(method, null)
        }
        client.newCall(builder.build()).execute().use { resp ->
            return HttpResult(resp.code, resp.header("x-user"), resp.body?.string() ?: "")
        }
    }
}
