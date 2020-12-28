package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class RiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val foregroundState: ForegroundState,
    private val notificationHelper: NotificationHelper
) {

    fun launch() {
        Timber.v("Monitoring risk level changes.")
        riskLevelStorage.latestRiskLevelResults
            .map { results ->
                results.sortedBy { it.calculatedAt }.takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for risklevel change.")
                check(it)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    private suspend fun check(changedLevels: List<RiskLevelResult>) {
        val oldResult = changedLevels.first()
        val newResult = changedLevels.last()

        val lastCheckedResult = riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.lastChangeCheckedRiskLevelTimestamp = newResult.calculatedAt

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState

        Timber.d("Last state was $oldRiskState and current state is $newRiskState")

        if (hasHighLowLevelChanged(oldRiskState, newRiskState) && !LocalData.submissionWasSuccessful()) {
            Timber.d("Notification Permission = ${notificationManagerCompat.areNotificationsEnabled()}")

            if (!foregroundState.isInForeground.first()) {
                notificationHelper.sendNotification(
                    content = context.getString(R.string.notification_body),
                    notificationId = NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                )
            } else {
                Timber.d("App is in foreground, not sending notifications")
            }

            Timber.d("Risk level changed and notification sent. Current Risk level is $newRiskState")
        }

        if (oldRiskState == RiskState.INCREASED_RISK && newRiskState == RiskState.LOW_RISK) {
            LocalData.isUserToBeNotifiedOfLoweredRiskLevel = true

            Timber.d("Risk level changed LocalData is updated. Current Risk level is $newRiskState")
        }
    }

    companion object {
        /**
         * Checks if the RiskLevel has change from a high to low or from low to high
         *
         * @param previous previously persisted RiskLevel
         * @param current newly calculated RiskLevel
         * @return
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun hasHighLowLevelChanged(previous: RiskState, current: RiskState) =
            previous.isIncreasedRisk != current.isIncreasedRisk

        private val RiskState.isIncreasedRisk: Boolean
            get() = this == RiskState.INCREASED_RISK
    }
}
