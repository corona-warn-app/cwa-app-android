package de.rki.coronawarnapp.util.retrofit

import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response

fun Headers.etag(): String? = values("ETag").singleOrNull()

val Response<ResponseBody>.wasModified: Boolean
    get() {
        val code = raw().networkResponse?.code
        return code != null && code != 304
    }
