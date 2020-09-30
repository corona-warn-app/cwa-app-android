package de.rki.coronawarnapp.verification.server

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VerificationApiV1 {

    data class RegistrationTokenRequest(
        @SerializedName("keyType") val keyType: String? = null,
        @SerializedName("key") val key: String? = null,
        @SerializedName("requestPadding") val requestPadding: String? = null
    )

    data class RegistrationTokenResponse(
        @SerializedName("registrationToken") val registrationToken: String
    )

    @POST("version/v1/registrationToken")
    suspend fun getRegistrationToken(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: RegistrationTokenRequest
    ): RegistrationTokenResponse

    data class RegistrationRequest(
        @SerializedName("registrationToken") val registrationToken: String? = null,
        @SerializedName("requestPadding") val requestPadding: String? = null
    )

    data class TestResultResponse(
        @SerializedName("testResult") val testResult: Int
    )

    @POST("version/v1/testresult")
    suspend fun getTestResult(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body request: RegistrationRequest
    ): TestResultResponse

    data class TanRequestBody(
        @SerializedName("registrationToken") val registrationToken: String? = null,
        @SerializedName("requestPadding") val requestPadding: String? = null
    )

    data class TanResponse(
        @SerializedName("tan") val tan: String
    )

    @POST("version/v1/tan")
    suspend fun getTAN(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: TanRequestBody
    ): TanResponse
}
