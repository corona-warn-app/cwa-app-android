package de.rki.coronawarnapp.ccl.configuration.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface CCLConfigurationApiV1 {

    @GET("/version/v1/ccl/config")
    suspend fun getCCLConfiguration(): Response<ResponseBody>
}
