package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DiagnosisKeyApiV1 {
    @GET("/version/v1/diagnosis-keys/country")
    suspend fun getLocationIndex(): List<String>

    @GET("/version/v1/diagnosis-keys/country/{country}/date")
    suspend fun getDayIndex(
        @Path("country") country: String
    ): List<String>

    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}/hour")
    suspend fun getHourIndex(
        @Path("country") country: String,
        @Path("day") day: String
    ): List<String>

    @Streaming
    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}")
    suspend fun downloadKeyFileForDay(
        @Path("country") country: String,
        @Path("day") day: String
    ): Response<ResponseBody>

    @Streaming
    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}/hour/{hour}")
    suspend fun downloadKeyFileForHour(
        @Path("country") country: String,
        @Path("day") day: String,
        @Path("hour") hour: String
    ): Response<ResponseBody>
}
