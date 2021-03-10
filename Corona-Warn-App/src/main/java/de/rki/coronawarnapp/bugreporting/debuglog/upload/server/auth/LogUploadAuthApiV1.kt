package de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import retrofit2.http.Body
import retrofit2.http.POST

interface LogUploadAuthApiV1 {

    data class AuthResponse(
        @SerializedName("expirationDate") val expirationDate: String
    )

    data class AuthError(
        @SerializedName("errorCode") val errorCode: String?
    )

    @POST("version/v1/android/els")
    suspend fun authOTP(
        @Body requestBody: ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid
    ): AuthResponse
}
