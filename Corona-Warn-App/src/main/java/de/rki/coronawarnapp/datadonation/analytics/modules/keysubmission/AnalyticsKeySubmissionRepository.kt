package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import org.joda.time.Duration
import javax.inject.Inject

class AnalyticsKeySubmissionRepository @Inject constructor(
    private val storage: AnalyticsKeySubmissionStorage
) {
    val testResultReceivedAt: Long
        get() = storage.testResultReceivedAt.value

    private val testRegisteredAt: Long
        get() = storage.testRegisteredAt.value

    val submitted: Boolean
        get() = storage.submitted.value

    private val submittedAt: Long
        get() = storage.submittedAt.value

    val submittedInBackground: Boolean
        get() = submitted && storage.submittedInBackground.value

    val submittedAfterCancel: Boolean
        get() = submitted && storage.submittedAfterCancel.value

    val submittedAfterSymptomFlow: Boolean
        get() = submitted && storage.submittedAfterSymptomFlow.value

    val submittedWithTeleTAN: Boolean
        get() = submitted && storage.registeredWithTeleTAN.value

    val lastSubmissionFlowScreen: Int
        get() = storage.lastSubmissionFlowScreen.value

    val advancedConsentGiven: Boolean
        get() = storage.advancedConsentGiven.value

    val hoursSinceTestResult: Int
        get() {
            if (submittedAt <= 0) return -1
            if (testResultReceivedAt <= 0) return -1
            if (submittedAt < testResultReceivedAt) return -1
            return Duration.millis(submittedAt - testResultReceivedAt).toStandardHours().hours
        }

    val hoursSinceTestRegistration: Int
        get() {
            if (submittedAt <= 0) return -1
            if (testRegisteredAt <= 0) return -1
            if (submittedAt < testRegisteredAt) return -1
            return Duration.millis(submittedAt - testRegisteredAt).toStandardHours().hours
        }

    val daysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int
        get() = storage.daysSinceMostRecentDateAtRiskLevelAtTestRegistration.value

    val hoursSinceHighRiskWarningAtTestRegistration: Int
        get() = storage.hoursSinceHighRiskWarningAtTestRegistration.value

    fun reset() = storage.clear()
}
