package de.rki.coronawarnapp.util.retrofit

import okhttp3.Headers

fun Headers.etag(): String? = values("ETag").singleOrNull()
