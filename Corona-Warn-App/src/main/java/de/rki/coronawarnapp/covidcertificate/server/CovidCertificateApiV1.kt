package de.rki.coronawarnapp.covidcertificate.server

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CovidCertificateApiV1 {

    data class PublicKeyUploadRequest(
        @SerializedName("registrationToken") val registrationToken: String,
        @SerializedName("publicKey") val publicKey: String
    )

    @POST("/version/v1/publicKey")
    suspend fun sendPublicKey(
        @Body requestBody: PublicKeyUploadRequest
    )

    data class ComponentsRequest(
        @SerializedName("registrationToken") val registrationToken: String,
    )

    data class ComponentsResponse(
        @SerializedName("dek") val dek: String,
        @SerializedName("dcc") val dcc: String
    )

    @POST("/version/v1/publicKey")
    suspend fun getComponents(
        @Body requestBody: ComponentsRequest
    ): Response<ComponentsResponse>
}
