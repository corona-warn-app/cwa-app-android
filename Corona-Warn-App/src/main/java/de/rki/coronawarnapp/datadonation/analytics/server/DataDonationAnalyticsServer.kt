package de.rki.coronawarnapp.datadonation.analytics.server

import dagger.Lazy
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataDonationAnalyticsServer @Inject constructor(
    private val api: Lazy<DataDonationAnalyticsApiV1>
) {

    suspend fun uploadAnalyticsData(ppaDataRequestAndroid: PpaDataRequestAndroid.PPADataRequestAndroid) {
        val response = api.get().submitAndroidAnalytics(ppaDataRequestAndroid)

        if (response.code() == 204) {
            return
        }

        if (response.code() == 400 || response.code() == 401 || response.code() == 403) {
            val body = response.body()
            if (body != null) {
                throw AnalyticsException(body.errorState, null)
            }
        }

        throw AnalyticsException("An unknown error occurred during the request", null)
    }
}
