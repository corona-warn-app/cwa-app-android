package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
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
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Suppress("LongParameterList")
class RiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val foregroundState: ForegroundState,
    private val notificationHelper: NotificationHelper,
    private val surveys: Surveys,
    private val submissionSettings: SubmissionSettings,
    private val tracingSettings: TracingSettings,
    private val testResultDonorSettings: TestResultDonorSettings
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

        // Check sending a notification when risk level changes
        checkSendingNotification(oldRiskState, newRiskState)

        // Save Survey related data based on the risk state
        saveSurveyRiskState(oldRiskState, newRiskState, newResult)

        // Save TestDonor risk level timestamps
        saveTestDonorRiskLevelAnalytics(newResult)
    }

    private fun saveTestDonorRiskLevelAnalytics(
        newRiskState: RiskLevelResult
    ) {
        // Save riskLevelTurnedRedTime if not already set before for high risk detection
        Timber.i("riskLevelTurnedRedTime:%s", testResultDonorSettings.riskLevelTurnedRedTime.value)
        if (testResultDonorSettings.riskLevelTurnedRedTime.value == null) {
            if (newRiskState.isIncreasedRisk) {
                testResultDonorSettings.riskLevelTurnedRedTime.update {
                    newRiskState.calculatedAt
                }
                Timber.i(
                    "newRiskState:%s, riskLevelTurnedRedTime:%s",
                    newRiskState.riskState,
                    newRiskState.calculatedAt
                )
            }
        }

        // Save most recent date of high or low risks
        if (newRiskState.riskState in listOf(RiskState.INCREASED_RISK, RiskState.LOW_RISK)) {
            val lastRiskEncounterAt: Instant? = if (newRiskState.isIncreasedRisk) {
                newRiskState.aggregatedRiskResult?.mostRecentDateWithHighRisk
            } else {
                newRiskState.aggregatedRiskResult?.mostRecentDateWithLowRisk
            }

            Timber.i(
                "newRiskState:%s, lastRiskEncounterAt:%s",
                newRiskState.riskState,
                lastRiskEncounterAt
            )

            testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel.update {
                lastRiskEncounterAt
            }
        }
    }

    private suspend fun checkSendingNotification(
        oldRiskState: RiskState,
        newRiskState: RiskState
    ) {
        if (hasHighLowLevelChanged(oldRiskState, newRiskState) && !submissionSettings.isSubmissionSuccessful) {
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
    }

    private fun saveSurveyRiskState(
        oldRiskState: RiskState,
        newRiskState: RiskState,
        newResult: RiskLevelResult
    ) {
        if (oldRiskState == RiskState.INCREASED_RISK && newRiskState == RiskState.LOW_RISK) {
            tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update { true }
            Timber.d("Risk level changed LocalData is updated. Current Risk level is $newRiskState")

            surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER)
        }

        if (oldRiskState == RiskState.LOW_RISK && newRiskState == RiskState.INCREASED_RISK) {
            riskLevelSettings.lastChangeToHighRiskLevelTimestamp = newResult.calculatedAt
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
