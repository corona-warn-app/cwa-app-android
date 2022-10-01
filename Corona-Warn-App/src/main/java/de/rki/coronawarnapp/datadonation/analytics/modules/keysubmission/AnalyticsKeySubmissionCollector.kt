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
import de.rki.coronawarnapp.util.toJavaInstant
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.first
import java.time.Duration
import javax.inject.Inject

class AnalyticsKeySubmissionCollector @Inject constructor(
    private val timeStamper: TimeStamper,
    private val analyticsSettings: AnalyticsSettings,
    private val pcrStorage: AnalyticsPCRKeySubmissionStorage,
    private val raStorage: AnalyticsRAKeySubmissionStorage,
    private val riskLevelStorage: RiskLevelStorage,
) {

    fun reset(type: BaseCoronaTest.Type) {
        type.storage.clear()
    }

    suspend fun reportPositiveTestResultReceived(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        // do not overwrite once set
        if (type.storage.testResultReceivedAt.value > 0) return
        type.storage.testResultReceivedAt.update { timeStamper.nowJavaUTC.toEpochMilli() }
    }

    suspend fun reportTestRegistered(type: BaseCoronaTest.Type) {
        if (isDisabled()) return

        val testRegisteredAt = timeStamper.nowJavaUTC
        type.storage.testRegisteredAt.update { testRegisteredAt.toEpochMilli() }

        val lastResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated

        if (lastResult.ewRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allEwRiskLevelResults
                .first()
                .getLastChangeToHighEwRiskBefore(testRegisteredAt)
                ?.let {
                    val hours = Duration.between(
                        it,
                        testRegisteredAt
                    ).toHours().toInt()
                    type.storage.ewHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }

        if (lastResult.ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
            riskLevelStorage.allPtRiskLevelResults
                .first()
                .getLastChangeToHighPtRiskBefore(testRegisteredAt)
                ?.let {
                    val hours = Duration.between(
                        it,
                        testRegisteredAt
                    ).toHours().toInt()
                    type.storage.ptHoursSinceHighRiskWarningAtTestRegistration.update {
                        hours
                    }
                }
        }

        type.storage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ewRiskLevelResult.mostRecentDateAtRiskState?.toJavaInstant()?.toLocalDateUtc(),
                testRegisteredAt.toLocalDateUtc()
            )
        }

        type.storage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                lastResult.ptRiskLevelResult.mostRecentDateAtRiskState,
                testRegisteredAt.toLocalDateUtc()
            )
        }
    }

    suspend fun reportSubmitted(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.submitted.update { true }
        type.storage.submittedAt.update { timeStamper.nowJavaUTC.toEpochMilli() }
    }

    suspend fun reportSubmittedInBackground(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.submittedInBackground.update { true }
    }

    suspend fun reportSubmittedAfterCancel(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.submittedAfterCancel.update { true }
    }

    suspend fun reportSubmittedAfterSymptomFlow(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.submittedAfterSymptomFlow.update { true }
    }

    suspend fun reportLastSubmissionFlowScreen(screen: Screen, type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.lastSubmissionFlowScreen.update { screen.code }
    }

    suspend fun reportAdvancedConsentGiven(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.advancedConsentGiven.update { true }
    }

    suspend fun reportConsentWithdrawn(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.advancedConsentGiven.update { false }
    }

    suspend fun reportRegisteredWithTeleTAN() {
        if (isDisabled()) return
        pcrStorage.registeredWithTeleTAN.update { true }
    }

    suspend fun reportSubmittedWithCheckIns(type: BaseCoronaTest.Type) {
        if (isDisabled()) return
        type.storage.submittedWithCheckIns.update { true }
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
