package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.Headers

data class DownloadInfo(
    val headers: Headers,
    val localMD5: String? = null
) {

    val serverMD5 by lazy { headers.getPayloadChecksumMD5() }

    private fun Headers.getPayloadChecksumMD5(): String? {
        // TODO EXPOSUREBACK-178
        val fileMD5 = values("ETag").singleOrNull()
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
