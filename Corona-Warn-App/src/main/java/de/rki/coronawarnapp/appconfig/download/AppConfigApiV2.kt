package de.rki.coronawarnapp.appconfig.download

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface AppConfigApiV2 {

    @GET("/version/v1/app_config_android")
    suspend fun getApplicationConfiguration(): Response<ResponseBody>
}
