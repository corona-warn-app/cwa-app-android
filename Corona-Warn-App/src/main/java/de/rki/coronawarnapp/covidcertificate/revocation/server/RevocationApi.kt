package de.rki.coronawarnapp.covidcertificate.revocation.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RevocationApi {

    @GET("/version/v1/dcc-rl/kid")
    suspend fun getRevocationKidList(): Response<ResponseBody>

    @GET("/version/v1/dcc-rl/{kid}{type}")
    suspend fun getRevocationKidTypeIndex(
        @Path("kid") kid: String,
        @Path("type") type: String
    ): Response<ResponseBody>

    @GET("/version/v1/dcc-rl/{kid}{type}/{x}/{y}/chunk")
    suspend fun getRevocationChunk(
        @Path("kid") kid: String,
        @Path("type") type: String,
        @Path("x") x: String,
        @Path("y") y: String
    ): Response<ResponseBody>
}
