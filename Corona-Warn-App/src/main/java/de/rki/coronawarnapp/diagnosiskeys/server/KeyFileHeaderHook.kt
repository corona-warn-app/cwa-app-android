package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.Headers

class KeyFileHeaderHook(
    private val onEval: suspend KeyFileHeaderHook.(Headers) -> Boolean
) : DiagnosisKeyServer.HeaderHook {

    override suspend fun validate(headers: Headers): Boolean = onEval(headers)

    fun Headers.getPayloadChecksumMD5(): String? {
        // TODO Ping backend regarding alternative checksum sources
        var fileMD5 = values("ETag").singleOrNull()
        // The hash from these headers doesn't match, TODO EXPOSUREBACK-178
//                var fileMD5 = headers.values("x-amz-meta-cwa-hash-md5").singleOrNull()
//                if (fileMD5 == null) {
//                    headers.values("x-amz-meta-cwa-hash").singleOrNull()
//                }
//                if (fileMD5 == null) { // Fallback
//                    fileMD5 = headers.values("ETag").singleOrNull()
//                }
        return fileMD5?.removePrefix("\"")?.removeSuffix("\"")
    }
}
