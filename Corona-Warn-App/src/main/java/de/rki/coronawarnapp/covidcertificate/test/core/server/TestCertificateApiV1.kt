package de.rki.coronawarnapp.covidcertificate.test.core.server

import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TestCertificateApiV1 {

    data class PublicKeyUploadRequest(
        @JsonProperty("registrationToken") val registrationToken: String,
        @JsonProperty("publicKey") val publicKey: String
    )

    @POST("/version/v1/publicKey")
    suspend fun sendPublicKey(
        @Body requestBody: PublicKeyUploadRequest
    ): Response<Unit>

    data class ComponentsRequest(
        @JsonProperty("registrationToken") val registrationToken: String,
    )

    data class ComponentsResponse(
        @JsonProperty("dek") val dek: String? = null,
        @JsonProperty("dcc") val dcc: String? = null,
        @JsonProperty("reason") val errorReason: String? = null
    ) {
        enum class Reason(val errorString: String) {
            SIGNING_CLIENT_ERROR("SIGNING_CLIENT_ERROR"),
            SIGNING_SERVER_ERROR("SIGNING_SERVER_ERROR"),
            LAB_INVALID_RESPONSE("LAB_INVALID_RESPONSE"),
            INTERNAL("INTERNAL")
        }
    }

    @POST("/version/v1/dcc")
    suspend fun getComponents(
        @Body requestBody: ComponentsRequest
    ): Response<ComponentsResponse?>
}
