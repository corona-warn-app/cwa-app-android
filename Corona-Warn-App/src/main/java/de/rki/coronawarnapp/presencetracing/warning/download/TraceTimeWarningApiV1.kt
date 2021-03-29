package de.rki.coronawarnapp.presencetracing.warning.download

import de.rki.coronawarnapp.presencetracing.warning.WarningPackageIds
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

private const val PATH = "/version/v1/twp/country/DE/hour"

interface TraceTimeWarningApiV1 {
    @GET(PATH)
    suspend fun getWarningPackageIds(
    ): Response<WarningPackageIds>

    @Streaming
    @GET("$PATH/{warningPackageId}")
    suspend fun downloadKeyFileForHour(
        @Path("warningPackageId") warningPackageId: Long
    ): Response<ResponseBody>
}

