package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RiskDetectionCanceller @Inject constructor(
    private val timeStamper: TimeStamper,
    private val enfClient: ENFClient,
    private val appConfigProvider: AppConfigProvider
) {

    private val internalLastCancelResult = MutableStateFlow(CancelResult.DONT_CANCEL)
    val lastCancelResult = internalLastCancelResult.asStateFlow()

    suspend fun shouldCancel(): CancelResult {

        val exposureConfig: ConfigData = appConfigProvider.getAppConfig()
        val trackedDetections = enfClient.latestTrackedExposureDetection().first()
        val now = timeStamper.nowUTC

        val lastDetection = trackedDetections.maxByOrNull { it.startedAt }
        if (lastDetection == null) {
            Timber.tag(TAG).d("No previous detections exist, don't cancel.")
            internalLastCancelResult.value = CancelResult.DONT_CANCEL
            return CancelResult.DONT_CANCEL
        }

        if (lastDetection.startedAt.isAfter(now.plus(Duration.standardHours(1)))) {
            Timber.tag(TAG).w("Last detection happened in our future? Don't cancel as precaution.")
            internalLastCancelResult.value = CancelResult.DONT_CANCEL
            return CancelResult.DONT_CANCEL
        }

        val nextDetectionAt = lastDetection.startedAt.plus(exposureConfig.minTimeBetweenDetections)

        Duration(now, nextDetectionAt).also {
            Timber.tag(TAG).d("Next detection is available in %d min", it.standardMinutes)
        }

        // At most one detection every 6h
        if (now.isBefore(nextDetectionAt)) {
            Timber.tag(TAG).w("Cancelling. Last detection is recent: %s (now=%s)", lastDetection, now)
            internalLastCancelResult.value = CancelResult.CANCEL_DUE_TO_RECENT_RISK_DETECTION
            return CancelResult.CANCEL_DUE_TO_RECENT_RISK_DETECTION
        }

        internalLastCancelResult.value = CancelResult.DONT_CANCEL
        return CancelResult.DONT_CANCEL
    }

    enum class CancelResult {
        DONT_CANCEL, CANCEL_DUE_TO_RECENT_RISK_DETECTION
    }

    companion object {
        private val TAG: String? = RiskDetectionCanceller::class.simpleName
    }
}
