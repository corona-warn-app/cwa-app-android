package de.rki.coronawarnapp.diagnosiskeys.server

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface AppConfigApiV1 {

    @GET("/version/v1/configuration/country/{country}/app_config")
    suspend fun getApplicationConfiguration(
        @Path("country") country: String
    ): ResponseBody
}
