package de.rki.coronawarnapp.dccticketing.core.decorator

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface DccTicketingDecoratorApi {

    @GET
    suspend fun getIdentityDocument(
        @Url url: String
    ): Response<ResponseBody>
}
