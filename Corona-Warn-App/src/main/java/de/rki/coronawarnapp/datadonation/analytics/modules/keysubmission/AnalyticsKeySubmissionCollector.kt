package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import javax.inject.Inject

class AnalyticsKeySubmissionCollector @Inject constructor(
    private val analyticsSettings: AnalyticsSettings,
    private val analyticsKeySubmissionStorage: AnalyticsKeySubmissionStorage
) {

    fun reset() {
        analyticsKeySubmissionStorage.clear()
    }

    fun reportPositiveTestResultReceived() {
    }

    fun reportTestRegistered(timestamp: Long) {
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

    fun reportAfterSymptomFlow() {
        if (isEnabled) analyticsKeySubmissionStorage.submittedAfterSymptomFlow.update { true }
    }

    fun reportLastSubmissionFlowScreen(screen: SubmissionFlowScreen) {
        if (isEnabled) analyticsKeySubmissionStorage.lastSubmissionFlowScreen.update { screen.code }
    }

    fun reportAdvancedConsentGiven() {
        if (isEnabled) analyticsKeySubmissionStorage.advancedConsentGiven.update { true }
    }

    fun reportRegisteredWithTeleTAN() {
        //if(isEnabled) analyticsKeySubmissionStorage.submittedWithTeleTAN.update { true }
    }

    private val isEnabled: Boolean
        get() = analyticsSettings.analyticsEnabled.value
}
