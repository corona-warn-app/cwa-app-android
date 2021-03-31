package de.rki.coronawarnapp.presencetracing.warning.download.server

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.InputStream

data class TraceTimeWarningDownload(val response: Response<ResponseBody>) {

    private val headers = response.headers()

    val etag by lazy { headers.values("ETag").singleOrNull() }

    val isEmptyPkg by lazy { headers.values("cwa-empty-pkg").singleOrNull() == "1" }

    fun readBody(): InputStream = requireNotNull(response.body()) { "Response body was null" }.byteStream()
}
