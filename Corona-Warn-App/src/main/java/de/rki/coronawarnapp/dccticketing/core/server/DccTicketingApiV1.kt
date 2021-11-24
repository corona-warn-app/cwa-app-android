package de.rki.coronawarnapp.dccticketing.core.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface DccTicketingApiV1 {

    @GET
    suspend fun getUrl(
        @Url url: String
    ): Response<ResponseBody>

    @Headers(
        "X-VERSION: 1.0.0",
        "Accept: application/jwt",
        "Content-Type: application/json",
    )
    @POST
    suspend fun getAccessToken(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
        @Body body: AccessTokenRequest
    ): Response<ResponseBody>

    @Headers(
        "X-VERSION: 1.0.0",
        "Accept: application/jwt",
        "Content-Type: application/json",
    )
    @POST
    suspend fun getResultToken(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
        @Body body: ResultTokenRequest
    ): Response<ResponseBody>
}
