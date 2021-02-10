package de.rki.coronawarnapp.datadonation.analytics.server

import dagger.Lazy
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsException
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataDonationAnalyticsServer @Inject constructor(
    private val api: Lazy<DataDonationAnalyticsApiV1>
) {

    suspend fun uploadAnalyticsData(ppaDataRequestAndroid: PpaDataRequestAndroid.PPADataRequestAndroid) {
        val response = api.get().submitAndroidAnalytics(ppaDataRequestAndroid)

        if (response.code() == 204) {
            Timber.d("Analytics upload completed successfully")
            return
        }

        if (response.code() == 400 || response.code() == 401 || response.code() == 403) {
            Timber.w("Analytics upload failed due to a known error, see exception")

            val body = response.body()
            if (body != null) {
                throw AnalyticsException(body.errorState, null)
            }
        }

        Timber.e("Analytics upload failed due to a unknown server side error")
        throw AnalyticsException("An unknown error occurred during the request", null)
    }
}
