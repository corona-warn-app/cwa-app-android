package de.rki.coronawarnapp.bugreporting.logupload.auth

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.ElsOtpRequestAndroid
import org.joda.time.Instant
import retrofit2.http.Body
import retrofit2.http.POST

interface LogUploadAuthApiV1 {

    data class AuthResponse(
        @SerializedName("expirationDate") val expirationDate: Instant
    )

    data class AuthError(
        @SerializedName("errorCode") val errorCode: String?
    )

    @POST("version/v1/android/log")
    suspend fun authOTP(
        @Body requestBody: ElsOtpRequestAndroid.ELSOneTimePasswordRequestAndroid
    ): AuthResponse
}
