package de.rki.coronawarnapp.presencetracing.warning.download.server

import okhttp3.ResponseBody
import retrofit2.Response

interface TraceWarningApi {
    suspend fun getWarningPackageIds(region: String): DiscoveryResult

    suspend fun downloadKeyFileForHour(region: String, timeId: Long): Response<ResponseBody>

    enum class Mode {
        UNENCRYPTED,
        ENCRYPTED
    }
}
