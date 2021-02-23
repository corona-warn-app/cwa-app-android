package de.rki.coronawarnapp.datadonation.survey.server

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtpRequestAndroid
import retrofit2.http.Body
import retrofit2.http.POST

interface SurveyApiV1 {

    data class DataDonationResponse(
        @SerializedName("errorCode") val errorCode: String?
    )

    @POST("version/v1/android/otp")
    suspend fun authOTP(
        @Body requestBody: EdusOtpRequestAndroid.EDUSOneTimePasswordRequestAndroid
    ): DataDonationResponse
}
