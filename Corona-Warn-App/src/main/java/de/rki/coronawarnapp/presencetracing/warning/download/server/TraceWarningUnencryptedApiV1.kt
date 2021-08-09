package de.rki.coronawarnapp.presencetracing.warning.download.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface TraceWarningUnencryptedApiV1 : TraceWarningApi {

    @GET("/version/v1/twp/country/{region}/hour")
    override suspend fun getWarningPackageIds(
        @Path("region") region: String
    ): DiscoveryResult

    @Streaming
    @GET("/version/v1/twp/country/{region}/hour/{timeId}")
    override suspend fun downloadKeyFileForHour(
        @Path("region") region: String,
        @Path("timeId") timeId: Long
    ): Response<ResponseBody>
}
