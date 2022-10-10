package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighEwRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighPtRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

class AnalyticsKeySubmissionCollector @Inject constructor(
    private val timeStamper: TimeStamper,
    private val analyticsSettings: AnalyticsSettings,
    private val pcrStorage: AnalyticsPCRKeySubmissionStorage,
    private val raStorage: AnalyticsRAKeySubmissionStorage,
    private val riskLevelStorage: RiskLevelStorage,
    @AppScope private val scope: CoroutineScope
) {

    fun reset(type: BaseCoronaTest.Type) {
        scope.launch {
            type.storage.clear()
        }
    }

    suspend fun reportPositiveTestResultReceived(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        // do not overwrite once set
        if (type.storage.testResultReceivedAt.first() > 0) return
        type.storage.updateTestResultReceivedAt(timeStamper.nowUTC.toEpochMilli())
    }

    suspend fun reportTestRegistered(type: BaseCoronaTest.Type) {
        if (isDisabled()) return

        val testRegisteredAt = timeStamper.nowUTC
        type.storage.updateTestRegisteredAt(testRegisteredAt.toEpochMilli())

        val lastResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated

        if (lastResult.ewRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allEwRiskLevelResults
                .first()
                .getLastChangeToHighEwRiskBefore(testRegisteredAt)
                ?.let {
                    val hours = Duration.between(it, testRegisteredAt).toHours().toInt()
                    type.storage.updateEwHoursSinceHighRiskWarningAtTestRegistration(hours)
                }
        }

        if (lastResult.ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allPtRiskLevelResults
                .first()
                .getLastChangeToHighPtRiskBefore(testRegisteredAt)
                ?.let {
                    val hours = Duration.between(it, testRegisteredAt).toHours().toInt()
                    type.storage.updatePtHoursSinceHighRiskWarningAtTestRegistration(hours)
                }
        }

        type.storage.updateEwDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ewRiskLevelResult.mostRecentDateAtRiskState?.toLocalDateUtc(),
                testRegisteredAt.toLocalDateUtc()
            )
        )

        type.storage.updatePtDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ptRiskLevelResult.mostRecentDateAtRiskState,
                testRegisteredAt.toLocalDateUtc()
            )
        )
    }

    suspend fun reportSubmitted(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateSubmitted(true)
        type.storage.updateSubmittedAt(timeStamper.nowUTC.toEpochMilli())
    }

    suspend fun reportSubmittedInBackground(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateSubmittedInBackground(true)
    }

    suspend fun reportSubmittedAfterCancel(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateSubmittedAfterCancel(true)
    }

    suspend fun reportSubmittedAfterSymptomFlow(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateSubmittedAfterSymptomFlow(true)
    }

    suspend fun reportLastSubmissionFlowScreen(screen: Screen, type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateLastSubmissionFlowScreen(screen.code)
    }

    suspend fun reportAdvancedConsentGiven(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateAdvancedConsentGiven(true)
    }

    suspend fun reportConsentWithdrawn(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateAdvancedConsentGiven(false)
    }

    suspend fun reportRegisteredWithTeleTAN() {
        if (isDisabled()) return
        pcrStorage.updateRegisteredWithTeleTAN(true)
    }

    suspend fun reportSubmittedWithCheckIns(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.updateSubmittedWithCheckIns(true)
    }

    private suspend fun isDisabled() = !analyticsSettings.analyticsEnabled.first()

    private val BaseCoronaTest.Type.storage: AnalyticsKeySubmissionStorage
        get() = when (this) {
            PCR -> pcrStorage
            RAPID_ANTIGEN -> raStorage
        }
}

enum class Screen(val code: Int) {
    UNKNOWN(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_UNKNOWN_VALUE),
    TEST_RESULT(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_TEST_RESULT_VALUE),
    WARN_OTHERS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_WARN_OTHERS_VALUE),
    SYMPTOMS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOMS_VALUE),
    SYMPTOM_ONSET(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOM_ONSET_VALUE)
}
