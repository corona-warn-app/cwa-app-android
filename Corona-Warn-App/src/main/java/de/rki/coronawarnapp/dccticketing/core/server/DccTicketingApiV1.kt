package de.rki.coronawarnapp.dccticketing.core.server

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface DccTicketingApiV1 {

    @GET
    suspend fun getServiceIdentityDocument(@Url url: String): DccTicketingServiceIdentityDocument
}
