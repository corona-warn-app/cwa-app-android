package de.rki.coronawarnapp.datadonation.survey.server

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import retrofit2.http.Body
import retrofit2.http.POST

interface SurveyApiV1 {

    data class DataDonationResponse(
        @SerializedName("expirationDate") val expirationDate: String?
    )

    @POST("version/v1/android/otp")
    suspend fun authOTP(
        @Body requestBody: EdusOtp.EDUSOneTimePassword
    ): DataDonationResponse
}
