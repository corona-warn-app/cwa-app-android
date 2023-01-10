package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import retrofit2.http.Body
import retrofit2.http.POST

interface LogUploadAuthApiV1 {

    data class AuthResponse(
        @JsonProperty("expirationDate") val expirationDate: String
    )

    data class AuthError(
        @JsonProperty("errorCode") val errorCode: String?
    )

    @POST("version/v1/android/els")
    suspend fun authOTP(
        @Body requestBody: ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid
    ): AuthResponse
}
