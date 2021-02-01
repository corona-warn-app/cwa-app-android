package de.rki.coronawarnapp.datadonation.analytics.server

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DataDonationAnalyticsApiV1 {
    @POST("/version/v1/android/dat")
    suspend fun submitAndroidAnalytics(
        @Body requestBody: PpaDataRequestAndroid.PPADataRequestAndroid
    ): Response<AndroidAnalyticsErrorResponse>
}
