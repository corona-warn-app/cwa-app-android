package de.rki.coronawarnapp.appconfig.sources.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AppConfigApiV1 {

    @GET("/version/v1/configuration/country/{country}/app_config")
    suspend fun getApplicationConfiguration(
        @Path("country") country: String
    ): Response<ResponseBody>
}
