package de.rki.coronawarnapp.dccreissuance.core.server

import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DccReissuanceApi {

    @POST("/api/certify/v2/reissue")
    suspend fun requestReissuance(
        @Body dccReissuanceRequestBody: DccReissuanceRequestBody
    ): Response<ResponseBody>
}
