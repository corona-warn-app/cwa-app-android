package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
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
        if (isEnabled) analyticsKeySubmissionStorage.testResultReceivedAt.update { timeStamper.nowUTC.millis }
    }

    suspend fun reportTestRegistered() {
        if (isDisabled) return
        val testRegisteredAt = timeStamper.nowUTC
        analyticsKeySubmissionStorage.testRegisteredAt.update { testRegisteredAt.millis }

        val lastRiskResult = riskLevelStorage
            .latestAndLastSuccessful
            .first()
            .tryLatestResultsWithDefaults()
            .lastCalculated
        val riskLevelAtRegistration = lastRiskResult.toMetadataRiskLevel()
        analyticsKeySubmissionStorage.riskLevelAtTestRegistration.update {
            riskLevelAtRegistration.number
        }

        if (riskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_HIGH) {
            riskLevelSettings.lastChangeToHighRiskLevelTimestamp?.let {
                val hours = Duration(
                    it,
                    testRegisteredAt
                ).standardHours.toInt()
                analyticsKeySubmissionStorage.hoursSinceHighRiskWarningAtTestRegistration.update {
                    hours
                }
            }
        }
    }

    fun reportSubmitted() {
        if (isEnabled) analyticsKeySubmissionStorage.submitted.update { true }
    }

    fun reportSubmittedInBackground() {
        if (isEnabled) analyticsKeySubmissionStorage.submittedInBackground.update { true }
    }

    fun reportSubmittedAfterCancel() {
        if (isEnabled) analyticsKeySubmissionStorage.submittedAfterCancel.update { true }
    }

    fun reportSubmittedAfterSymptomFlow() {
        if (isEnabled) analyticsKeySubmissionStorage.submittedAfterSymptomFlow.update { true }
    }

    fun reportLastSubmissionFlowScreen(screen: Screen) {
        if (isEnabled) analyticsKeySubmissionStorage.lastSubmissionFlowScreen.update { screen.code }
    }

    fun reportAdvancedConsentGiven() {
        if (isEnabled) analyticsKeySubmissionStorage.advancedConsentGiven.update { true }
    }

    fun reportConsentWithdrawn() {
        if (isEnabled) analyticsKeySubmissionStorage.advancedConsentGiven.update { false }
    }

    fun reportRegisteredWithTeleTAN() {
        if (isEnabled) analyticsKeySubmissionStorage.registeredWithTeleTAN.update { true }
    }

    private val isEnabled: Boolean
        get() = analyticsSettings.analyticsEnabled.value

    private val isDisabled: Boolean
        get() = !isEnabled
}

enum class Screen(val code: Int) {
    UNKNOWN(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_UNKNOWN_VALUE),
    TEST_RESULT(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_TEST_RESULT_VALUE),
    WARN_OTHERS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_WARN_OTHERS_VALUE),
    SYMPTOMS(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOMS_VALUE),
    SYMPTOM_ONSET(PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_SYMPTOM_ONSET_VALUE)
}
