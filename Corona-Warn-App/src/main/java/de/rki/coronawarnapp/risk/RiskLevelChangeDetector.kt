package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class RiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    fun launch() {
        Timber.v("Monitoring config changes.")
        riskLevelStorage.riskLevelResults
            .map { results -> results.sortedBy { it.calculatedAt }.takeLast(2) }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for risklevel change.")
                check(it)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    @VisibleForTesting
    internal fun check(changedLevels: List<RiskLevelResult>) {
        val oldResult = changedLevels.first()
        val newResult = changedLevels.last()

        val oldRiskLevel = oldResult.riskLevel
        val newRiskLevel = newResult.riskLevel

        Timber.d("last CalculatedS core is ${oldRiskLevel.raw} and Current Risk Level is ${newRiskLevel.raw}")

        if (riskLevelChangedBetweenLowAndHigh(oldRiskLevel, newRiskLevel) && !LocalData.submissionWasSuccessful()) {
            Timber.d("Notification Permission = ${notificationManagerCompat.areNotificationsEnabled()}")

            // TODO Static access bad!
            NotificationHelper.sendNotification(context.getString(R.string.notification_body))

            Timber.d("Risk level changed and notification sent. Current Risk level is $newRiskLevel")
        }

        if (oldRiskLevel.raw == RiskLevelConstants.INCREASED_RISK && newRiskLevel.raw == RiskLevelConstants.LOW_LEVEL_RISK) {
            LocalData.isUserToBeNotifiedOfLoweredRiskLevel = true

            Timber.d("Risk level changed LocalData is updated. Current Risk level is $newRiskLevel")
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
        private fun riskLevelChangedBetweenLowAndHigh(previous: RiskLevel, current: RiskLevel): Boolean {
            return HIGH_RISK_LEVELS.contains(previous) && LOW_RISK_LEVELS.contains(current) ||
                LOW_RISK_LEVELS.contains(previous) && HIGH_RISK_LEVELS.contains(current)
        }

        private val HIGH_RISK_LEVELS = arrayOf(RiskLevel.INCREASED_RISK)
        private val LOW_RISK_LEVELS = arrayOf(
            RiskLevel.UNKNOWN_RISK_INITIAL,
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            RiskLevel.LOW_LEVEL_RISK,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL,
            RiskLevel.UNDETERMINED
        )
    }
}
