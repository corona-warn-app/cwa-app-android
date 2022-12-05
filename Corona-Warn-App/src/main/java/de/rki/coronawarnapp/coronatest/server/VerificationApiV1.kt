package de.rki.coronawarnapp.coronatest.server

import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VerificationApiV1 {

    data class RegistrationTokenRequest(
        @JsonProperty("keyType") val keyType: VerificationKeyType,
        @JsonProperty("key") val key: String,
        @JsonProperty("dateOfBirthKey") val dateOfBirthKey: String? = null,
        @JsonProperty("requestPadding") val requestPadding: String? = null,
    )

    data class RegistrationTokenResponse(
        @JsonProperty("registrationToken") val registrationToken: String
    )

    @POST("version/v1/registrationToken")
    suspend fun getRegistrationToken(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: RegistrationTokenRequest
    ): RegistrationTokenResponse

    data class RegistrationRequest(
        @JsonProperty("registrationToken") val registrationToken: String,
        @JsonProperty("requestPadding") val requestPadding: String
    )

    data class TestResultResponse(
        @JsonProperty("testResult") val testResult: Int,
        @JsonProperty("sc") val sampleCollectedAt: Int?,
        @JsonProperty("labId") val labId: String?
    )

    @POST("version/v1/testresult")
    suspend fun getTestResult(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body request: RegistrationRequest
    ): TestResultResponse

    data class TanRequestBody(
        @JsonProperty("registrationToken") val registrationToken: String,
        @JsonProperty("requestPadding") val requestPadding: String
    )

    data class TanResponse(
        @JsonProperty("tan") val tan: String
    )

    @POST("version/v1/tan")
    suspend fun getTAN(
        @Header("cwa-fake") fake: String,
        @Header("cwa-header-padding") headerPadding: String?,
        @Body requestBody: TanRequestBody
    ): TanResponse
}
