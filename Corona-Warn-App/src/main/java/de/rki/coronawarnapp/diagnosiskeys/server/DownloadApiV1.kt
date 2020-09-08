package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DownloadApiV1 {

    @GET("/version/v1/configuration/country/{country}/app_config")
    suspend fun getApplicationConfiguration(@Path("country") country: String): ResponseBody

    @GET("/version/v1/diagnosis-keys/country")
    suspend fun getCountryIndex(): List<String> // TODO Let retrofit format this to CountryCode

    @GET("/version/v1/diagnosis-keys/country/{country}/date")
    suspend fun getDayIndex(@Path("country") country: String): List<String> // TODO Let retrofit format this to LocalDate

    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}/hour")
    suspend fun getHourIndex(
        @Path("country") country: String,
        @Path("day") day: String
    ): List<String> // TODO Let retrofit format this to LocalTime

    @Streaming
    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}")
    suspend fun downloadKeyFileForDay(
        @Path("country") country: String,
        @Path("day") day: String
    ): ResponseBody

    @Streaming
    @GET("/version/v1/diagnosis-keys/country/{country}/date/{day}/hour/{hour}")
    suspend fun downloadKeyFileForHour(
        @Path("country") country: String,
        @Path("day") day: String,
        @Path("hour") hour: String
    ): ResponseBody

}
