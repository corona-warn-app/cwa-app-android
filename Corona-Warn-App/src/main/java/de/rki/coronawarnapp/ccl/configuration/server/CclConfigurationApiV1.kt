package de.rki.coronawarnapp.ccl.configuration.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface CclConfigurationApiV1 {

    @GET("/version/v1/ccl/config-v2")
    suspend fun getCclConfiguration(): Response<ResponseBody>
}
