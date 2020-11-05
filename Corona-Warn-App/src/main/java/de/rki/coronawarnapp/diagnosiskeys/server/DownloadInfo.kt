package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.Headers

data class DownloadInfo(
    val headers: Headers
) {

    val etag by lazy { headers.getETag() }

    val etagWithoutQuotes: String?
        get() = etag?.removePrefix("\"")?.removeSuffix("\"")

    private fun Headers.getETag(): String? = values("ETag").singleOrNull()
}
