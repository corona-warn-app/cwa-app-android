package de.rki.coronawarnapp.presencetracing.warning.download.server

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.InputStream

data class TraceWarningPackageDownload(val response: Response<ResponseBody>) {

    private val headers = response.headers()

    val etag by lazy { headers.values("ETag").singleOrNull() }

    val isEmptyPkg by lazy { headers.values("Content-Length").singleOrNull() == "0" }

    fun readBody(): InputStream = requireNotNull(response.body()) { "Response body was null" }.byteStream()
}
