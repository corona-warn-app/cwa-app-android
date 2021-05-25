package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.getLastCalculatedWithDefaults
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestEwResultsWithDefaults
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.TriStateBooleanOuterClass
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import javax.inject.Inject

class AnalyticsKeySubmissionCollector @Inject constructor(
    private val timeStamper: TimeStamper,
    private val analyticsSettings: AnalyticsSettings,
    private val analyticsKeySubmissionStorage: AnalyticsKeySubmissionStorage,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings
) {

    fun reset() {
        analyticsKeySubmissionStorage.clear()
    }

    fun reportPositiveTestResultReceived() {
        if (disabled) return
        analyticsKeySubmissionStorage.testResultReceivedAt.update { timeStamper.nowUTC.millis }
    }

    suspend fun reportTestRegistered() {
        if (disabled) return
        val testRegisteredAt = timeStamper.nowUTC
        analyticsKeySubmissionStorage.testRegisteredAt.update { testRegisteredAt.millis }

        val lastEwRiskResult = riskLevelStorage
            .latestAndLastSuccessfulEwRiskLevelResult
            .first()
            .tryLatestEwResultsWithDefaults()
            .lastCalculated

        val lastPtRiskResult = riskLevelStorage
            .latestPtRiskLevelResults
            .first()
            .getLastCalculatedWithDefaults()

        val ewRiskLevelAtRegistration = lastEwRiskResult.toMetadataRiskLevel()
        analyticsKeySubmissionStorage.ewRiskLevelAtTestRegistration.update {
            ewRiskLevelAtRegistration.number
        }

        val ptRiskLevelAtRegistration = lastPtRiskResult.toMetadataRiskLevel()
        analyticsKeySubmissionStorage.ptRiskLevelAtTestRegistration.update {
            ptRiskLevelAtRegistration.number
        }

        if (ewRiskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_HIGH) {
            riskLevelSettings.lastChangeToHighEwRiskLevelTimestamp?.let {
                val hours = Duration(
                    it,
                    testRegisteredAt
                ).standardHours.toInt()
                analyticsKeySubmissionStorage.ewHoursSinceHighRiskWarningAtTestRegistration.update {
                    hours
                }
            }
        }

        if (ptRiskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_HIGH) {
            riskLevelSettings.lastChangeToHighPtRiskLevelTimestamp?.let {
                val hours = Duration(
                    it,
                    testRegisteredAt
                ).standardHours.toInt()
                analyticsKeySubmissionStorage.ptHoursSinceHighRiskWarningAtTestRegistration.update {
                    hours
                }
            }
        }

        analyticsKeySubmissionStorage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                riskLevelSettings.lastChangeCheckedEwRiskLevelTimestamp,
                testRegisteredAt
            )
        }

        analyticsKeySubmissionStorage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.update {
            calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                riskLevelSettings.lastChangeCheckedPtRiskLevelTimestamp,
                testRegisteredAt
            )
        }
    }

    fun reportSubmitted(withCheckins: Boolean) {
        if (disabled) return
        analyticsKeySubmissionStorage.submitted.update { true }
        analyticsKeySubmissionStorage.submittedAt.update { timeStamper.nowUTC.millis }
        analyticsKeySubmissionStorage.submittedWithCheckins.update { withCheckins.asTsb }
    }

    fun reportSubmittedInBackground() {
        if (disabled) return
        analyticsKeySubmissionStorage.submittedInBackground.update { true }
    }

    fun reportSubmittedAfterCancel() {
        if (disabled) return
        analyticsKeySubmissionStorage.submittedAfterCancel.update { true }
    }

    fun reportSubmittedAfterSymptomFlow() {
        if (disabled) return
        analyticsKeySubmissionStorage.submittedAfterSymptomFlow.update { true }
    }

    fun reportLastSubmissionFlowScreen(screen: Screen) {
        if (disabled) return
        analyticsKeySubmissionStorage.lastSubmissionFlowScreen.update { screen.code }
    }

    fun reportAdvancedConsentGiven() {
        if (disabled) return
        analyticsKeySubmissionStorage.advancedConsentGiven.update { true }
    }

    fun reportConsentWithdrawn() {
        if (disabled) return
        analyticsKeySubmissionStorage.advancedConsentGiven.update { false }
    }

    fun reportRegisteredWithTeleTAN() {
        if (disabled) return
        analyticsKeySubmissionStorage.registeredWithTeleTAN.update { true }
    }

    private val disabled: Boolean
        get() = !analyticsSettings.analyticsEnabled.value

    private val Boolean.asTsb: TriStateBooleanOuterClass.TriStateBoolean
        get() = when (this) {
            true -> TriStateBooleanOuterClass.TriStateBoolean.TSB_TRUE
            false -> TriStateBooleanOuterClass.TriStateBoolean.TSB_FALSE
        }
}

enum class Screen(val code: Int) {
    UNKNOWN(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_UNKNOWN_VALUE),
    TEST_RESULT(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_TEST_RESULT_VALUE),
    WARN_OTHERS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_WARN_OTHERS_VALUE),
    SYMPTOMS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOMS_VALUE),
    SYMPTOM_ONSET(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOM_ONSET_VALUE)
}
