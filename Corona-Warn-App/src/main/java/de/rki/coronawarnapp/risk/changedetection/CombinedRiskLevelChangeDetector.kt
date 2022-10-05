package de.rki.coronawarnapp.risk.changedetection

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskCardDisplayInfo
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/**
 * Checks for changes of low-to-high risks, high-to-low risks and additional high-risks after an initial high-risk
 **/
@Suppress("LongParameterList")
class CombinedRiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationHelper: GeneralNotifications,
    private val tracingSettings: TracingSettings,
    private val riskCardDisplayInfo: RiskCardDisplayInfo
) : Initializer {

    override fun initialize() {
        Timber.v("Monitoring combined risk level changes.")

        // send notifications when risk changes from LOW to HIGH or HIGH TO LOW
        riskLevelStorage.allCombinedEwPtRiskLevelResults
            .map { results ->
                results
                    .filter { it.wasSuccessfullyCalculated }
                    .sortedBy { it.calculatedAt }
                    .takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach { results ->
                Timber.v("Checking for low-to-high or high-to-low risklevel change.")
                results.checkForRiskLevelChanges()
            }
            .catch { Timber.e(it, "RiskLevel checks failed.") }
            .launchIn(appScope)

        // send notifications for additional HIGH risks after an initial HIGH risk
        riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .map { it.lastCalculated }
            .filter { it.lastRiskEncounterAt != null }
            .onEach { riskResult ->
                Timber.d("Checking for additional high risk after an initial high risk")
                riskResult.checkForAdditionalHighRisks()
            }.catch { Timber.e(it, "RiskLevel checks failed.") }
            .launchIn(appScope)
    }

    private suspend fun List<CombinedEwPtRiskLevelResult>.checkForRiskLevelChanges() {
        if (isEmpty()) return

        val oldResult = minByOrNull { it.calculatedAt }
        val newResult = maxByOrNull { it.calculatedAt }

        if (oldResult == null || newResult == null) return

        val lastCheckedResult = riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp.first()
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.updateLastChangeCheckedRiskLevelCombinedTimestamp(newResult.calculatedAt)

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState

        Timber.d("Last combined state was $oldRiskState and current state is $newRiskState")

        if (oldResult.riskState.hasChangedFromHighToLow(newResult.riskState)) {
            Timber.d("RiskState changed from high to low")

            // only show a notification if the low risk card is also shown
            val showNotification = riskCardDisplayInfo.shouldShowRiskCard(RiskState.LOW_RISK)
            Timber.d("Notification should be shown = %s", showNotification)

            if (showNotification) {
                sendNotification()
                showBadge()
                tracingSettings.updateUserToBeNotifiedOfLoweredRiskLevel(true)
            }

            Timber.d("Risk level changed LocalData is updated. Current Risk level is ${newResult.riskState}")
        } else if (oldRiskState.hasChangedFromLowToHigh(newRiskState)) {
            Timber.d("RiskState changed from low to high")
            sendNotification()
            showBadge()
        }
    }

    private suspend fun CombinedEwPtRiskLevelResult.checkForAdditionalHighRisks() {
        Timber.d(
            "New Risk State: $riskState " +
                "with lastRiskEncounterAt: $lastRiskEncounterAt"
        )
        val lastHighRiskDate = tracingSettings.lastHighRiskDate.first()
        Timber.d("Last high risk date: $lastHighRiskDate")
        when (riskState) {
            RiskState.INCREASED_RISK -> {
                when (lastHighRiskDate) {
                    null -> {
                        Timber.d("initial HIGH risk - no notification")
                        tracingSettings.updateLastHighRiskDate(date = lastRiskEncounterAt)
                    }
                    else -> {
                        if (lastRiskEncounterAt!!.isAfter(lastHighRiskDate)) {
                            Timber.d("additional HIGH risk - trigger notification")
                            sendNotification()
                            tracingSettings.updateUserToBeNotifiedOfAdditionalHighRiskLevel(notify = true)
                            tracingSettings.updateLastHighRiskDate(date = lastRiskEncounterAt)
                        } else {
                            Timber.d("HIGH risk is not newer than the stored one - do nothing")
                        }
                    }
                }
            }
            RiskState.LOW_RISK -> {
                if (lastHighRiskDate == null) {
                    Timber.d("lastHighRiskDate is null, do nothing")
                    return
                }
                if (lastRiskEncounterAt!!.isAfter(lastHighRiskDate)) {
                    Timber.d("LOW risk - Resetting lastHighRiskDate")
                    tracingSettings.updateLastHighRiskDate(date = null)
                    tracingSettings.updateUserToBeNotifiedOfAdditionalHighRiskLevel(notify = false)
                } else {
                    Timber.d("LOW risk before HIGH risk - do nothing")
                }
            }
            RiskState.CALCULATION_FAILED -> {
                Timber.d("Calculation failed - do nothing")
            }
        }
    }

    private fun sendNotification() {
        Timber.d("Notification Permission = ${notificationManagerCompat.areNotificationsEnabled()}")

        val notification = notificationHelper.newBaseBuilder()
            .setContentTitle(context.getString(R.string.notification_headline))
            .setContentTextExpandable(context.getString(R.string.notification_body))
            .build()

        notificationHelper.sendNotification(
            notificationId = NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID,
            notification = notification,
        )
    }

    private suspend fun showBadge() {
        tracingSettings.updateShowRiskLevelBadge(show = true)
    }
}
