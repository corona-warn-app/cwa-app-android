package de.rki.coronawarnapp.dccticketing.core.server

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface DccTicketingApiV1 {

    @GET
    suspend fun getServiceIdentityDocument(@Url url: String): DccTicketingServiceIdentityDocument

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
    ): Response<String>
}
