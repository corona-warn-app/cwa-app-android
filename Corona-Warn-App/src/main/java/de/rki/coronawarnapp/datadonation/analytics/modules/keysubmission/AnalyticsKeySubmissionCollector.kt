package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.util.TimeStamper
import javax.inject.Inject

class AnalyticsKeySubmissionCollector @Inject constructor(
    private val timeStamper: TimeStamper,
    private val analyticsSettings: AnalyticsSettings,
    private val analyticsKeySubmissionStorage: AnalyticsKeySubmissionStorage
) {

    fun reset() {
        analyticsKeySubmissionStorage.clear()
    }

    fun reportPositiveTestResultReceived() {
        analyticsKeySubmissionStorage.testResultReceivedAt.update { timeStamper.nowUTC.millis }
    }

    fun reportTestRegistered() {
        analyticsKeySubmissionStorage.testRegisteredAt.update { timeStamper.nowUTC.millis }
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

    fun reportLastSubmissionFlowScreen(screen: Int) {
        if (isEnabled) analyticsKeySubmissionStorage.lastSubmissionFlowScreen.update { screen }
    }

    fun reportAdvancedConsentGiven() {
        if (isEnabled) analyticsKeySubmissionStorage.advancedConsentGiven.update { true }
    }

    fun reportRegisteredWithTeleTAN() {
        if (isEnabled) analyticsKeySubmissionStorage.registeredWithTeleTAN.update { true }
    }

    private val isEnabled: Boolean
        get() = analyticsSettings.analyticsEnabled.value
}

const val SUBMISSION_FLOW_SCREEN_UNKNOWN = 0
const val SUBMISSION_FLOW_SCREEN_TEST_RESULT = 2
const val SUBMISSION_FLOW_SCREEN_WARN_OTHERS = 3
const val SUBMISSION_FLOW_SCREEN_SYMPTOMS = 4
const val SUBMISSION_FLOW_SCREEN_SYMPTOM_ONSET = 5

