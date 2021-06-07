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
    ): Response<Unit>

    data class ComponentsRequest(
        @SerializedName("registrationToken") val registrationToken: String,
    )

    data class ComponentsResponse(
        @SerializedName("dek") val dek: String? = null,
        @SerializedName("dcc") val dcc: String? = null,
        @SerializedName("reason") val errorReason: String? = null
    ) {
        enum class Reason(val errorString: String) {
            SIGNING_CLIENT_ERROR("SIGNING_CLIENT_ERROR"),
            SIGNING_SERVER_ERROR("SIGNING_SERVER_ERROR"),
            LAB_INVALID_RESPONSE("LAB_INVALID_RESPONSE"),
            INTERNAL("INTERNAL")
        }
    }

    @POST("/version/v1/publicKey")
    suspend fun getComponents(
        @Body requestBody: ComponentsRequest
    ): Response<ComponentsResponse>
}
