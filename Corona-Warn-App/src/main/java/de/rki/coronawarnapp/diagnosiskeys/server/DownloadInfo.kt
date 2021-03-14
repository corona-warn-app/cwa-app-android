package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.Headers

data class DownloadInfo(val headers: Headers) {

    val etag by lazy { headers.values("ETag").singleOrNull() }
}
