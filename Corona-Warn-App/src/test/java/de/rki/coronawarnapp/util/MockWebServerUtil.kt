package de.rki.coronawarnapp.util

import okhttp3.mockwebserver.RecordedRequest
import okio.utf8Size

fun RecordedRequest.requestHeaderWithoutContentLength() =
    listOf(this.requestLine)
        .plus(
            this.headers.filter { (k, _) -> k != "Content-Length" }.toString()
        )
        .joinToString("\n")

fun RecordedRequest.headerSizeIgnoringContentLength() =
    requestHeaderWithoutContentLength().utf8Size()
