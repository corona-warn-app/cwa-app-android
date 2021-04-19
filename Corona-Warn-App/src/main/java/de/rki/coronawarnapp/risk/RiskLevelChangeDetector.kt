package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
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

@Suppress("LongParameterList")
class RiskLevelChangeDetector @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val foregroundState: ForegroundState,
    private val notificationHelper: GeneralNotifications,
    private val surveys: Surveys,
    private val coronaTestRepository: CoronaTestRepository,
    private val tracingSettings: TracingSettings,
    private val testResultDonorSettings: TestResultDonorSettings
) {

    fun launch() {
        Timber.v("Monitoring risk level changes.")
        riskLevelStorage.latestEwRiskLevelResults
            .map { results ->
                results.sortedBy { it.calculatedAt }.takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for ew risklevel change.")
                checkEwRiskForStateChanges(it)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)

        riskLevelStorage.latestCombinedEwPtRiskLevelResults
            .map { results ->
                results.sortedBy { it.calculatedAt }.takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for combined risklevel change.")
                checkCombinedRiskForStateChanges(it)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    private suspend fun checkCombinedRiskForStateChanges(results: List<CombinedEwPtRiskLevelResult>) {
        val oldResult = results.first()
        val newResult = results.last()

        val lastCheckedResult = riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.lastChangeCheckedRiskLevelCombinedTimestamp = newResult.calculatedAt

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState
        Timber.d("Last combined state was $oldRiskState and current state is $newRiskState")

        // Check sending a notification when risk level changes
        checkSendingNotification(oldRiskState, newRiskState)
    }

    private fun checkEwRiskForStateChanges(results: List<EwRiskLevelResult>) {
        val oldResult = results.first()
        val newResult = results.last()

        val lastCheckedResult = riskLevelSettings.lastChangeCheckedRiskLevelTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.lastChangeCheckedRiskLevelTimestamp = newResult.calculatedAt

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState
        Timber.d("Last state was $oldRiskState and current state is $newRiskState")

        // Save Survey related data based on the risk state
        saveSurveyRiskState(oldRiskState, newRiskState, newResult)

        // Save TestDonor risk level timestamps
        saveTestDonorRiskLevelAnalytics(newResult)
    }

    private fun saveTestDonorRiskLevelAnalytics(
        newEwRiskState: EwRiskLevelResult
    ) {
        // Save riskLevelTurnedRedTime if not already set before for high risk detection
        Timber.i("riskLevelTurnedRedTime=%s", testResultDonorSettings.riskLevelTurnedRedTime.value)
        if (testResultDonorSettings.riskLevelTurnedRedTime.value == null) {
            if (newEwRiskState.isIncreasedRisk) {
                testResultDonorSettings.riskLevelTurnedRedTime.update {
                    newEwRiskState.calculatedAt
                }
                Timber.i(
                    "riskLevelTurnedRedTime: newRiskState=%s, riskLevelTurnedRedTime=%s",
                    newEwRiskState.riskState,
                    newEwRiskState.calculatedAt
                )
            }
        }

        // Save most recent date of high or low risks
        if (newEwRiskState.riskState in listOf(RiskState.INCREASED_RISK, RiskState.LOW_RISK)) {
            Timber.d("newRiskState=$newEwRiskState")
            val lastRiskEncounterAt = newEwRiskState.lastRiskEncounterAt
            Timber.i(
                "mostRecentDateWithHighOrLowRiskLevel: newRiskState=%s, lastRiskEncounterAt=%s",
                newEwRiskState.riskState,
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
        val isSubmissionSuccessful = coronaTestRepository.coronaTests.first().any { it.isSubmitted }
        if (hasHighLowLevelChanged(oldRiskState, newRiskState) && !isSubmissionSuccessful) {
            Timber.d("Notification Permission = ${notificationManagerCompat.areNotificationsEnabled()}")

            if (!foregroundState.isInForeground.first()) {
                val notification = notificationHelper.newBaseBuilder().apply {
                    setContentTitle(context.getString(R.string.notification_headline))
                    setContentTextExpandable(context.getString(R.string.notification_body))
                }.build()

                notificationHelper.sendNotification(
                    notificationId = NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID,
                    notification = notification,
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
        newResult: EwRiskLevelResult
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
