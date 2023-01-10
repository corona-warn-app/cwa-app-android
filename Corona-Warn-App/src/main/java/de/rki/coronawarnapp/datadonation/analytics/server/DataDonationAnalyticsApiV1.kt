package de.rki.coronawarnapp.datadonation.analytics.server

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DataDonationAnalyticsApiV1 {

    data class DataDonationAnalyticsResponse(
        @JsonProperty("errorCode") val errorCode: String?
    )

    @POST("version/v1/android/dat")
    suspend fun submitAndroidAnalytics(
        @Body requestBody: PpaDataRequestAndroid.PPADataRequestAndroid
    ): Response<DataDonationAnalyticsResponse>
}
