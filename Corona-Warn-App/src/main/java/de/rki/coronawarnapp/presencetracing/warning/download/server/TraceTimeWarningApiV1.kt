package de.rki.coronawarnapp.presencetracing.warning.download.server

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.HourInterval
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface TraceTimeWarningApiV1 {

    @Keep
    data class DiscoveryResult(
        @SerializedName("oldest") val oldest: HourInterval,
        @SerializedName("latest") val latest: HourInterval
    )

    @GET("/version/v1/twp/country/{region}/hour")
    suspend fun getWarningPackageIds(
        @Path("region") region: String
    ): DiscoveryResult

    @Streaming
    @GET("/version/v1/twp/country/{region}/hour/{timeId}")
    suspend fun downloadKeyFileForHour(
        @Path("region") region: String,
        @Path("timeId") timeId: Long
    ): Response<ResponseBody>
}

