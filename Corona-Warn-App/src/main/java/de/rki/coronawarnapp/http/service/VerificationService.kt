package de.rki.coronawarnapp.http.service

import de.rki.coronawarnapp.http.requests.RegistrationTokenRequest
import de.rki.coronawarnapp.http.requests.ReqistrationRequest
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.http.responses.RegistrationTokenResponse
import de.rki.coronawarnapp.http.responses.TanResponse
import de.rki.coronawarnapp.http.responses.TestResultResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface VerificationService {

    @POST
    suspend fun getRegistrationToken(
        @Url url: String,
        @Header("cwa-fake") fake: String,
        @Body requestBody: RegistrationTokenRequest
    ): RegistrationTokenResponse

    @POST
    suspend fun getTestResult(
        @Url url: String,
        @Header("cwa-fake") fake: String,
        @Body request: ReqistrationRequest
    ): TestResultResponse

    @POST
    suspend fun getTAN(
        @Url url: String,
        @Header("cwa-fake") fake: String,
        @Body requestBody: TanRequestBody
    ): TanResponse
}
