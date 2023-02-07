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

        val code = response.code().also {
            Timber.d("Response code: %d", it)
        }

        return when {
            response.isSuccessful -> Timber.d("Analytics upload completed successfully")
            code in listOf(400, 401, 403) -> {
                val explanation = response.body()?.errorCode ?: "Unknown clientside error"
                throw AnalyticsException(message = explanation).also {
                    Timber.w(it, "Analytics upload failed with 40X")
                }
            }

            else -> {
                throw AnalyticsException(message = "An unknown error occurred during the request").also {
                    Timber.e(it, "Analytics upload failed due to a unknown error")
                }
            }
        }
    }
}
