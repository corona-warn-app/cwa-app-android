package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsPCRKeySubmissionRepository @Inject constructor(
    storage: AnalyticsPCRKeySubmissionStorage
) : AnalyticsKeySubmissionRepository(storage) {
    override val submittedAfterRAT: Boolean = false
}

@Singleton
class AnalyticsRAKeySubmissionRepository @Inject constructor(
    storage: AnalyticsRAKeySubmissionStorage
) : AnalyticsKeySubmissionRepository(storage) {
    override val submittedAfterRAT: Boolean = true
}

abstract class AnalyticsKeySubmissionRepository(
    private val storage: AnalyticsKeySubmissionStorage
) {
    abstract val submittedAfterRAT: Boolean

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
            return Duration.ofMillis(submittedAt - testResultReceivedAt).toHours().toInt()
        }

    val hoursSinceTestRegistration: Int
        get() {
            if (submittedAt <= 0) return -1
            if (testRegisteredAt <= 0) return -1
            if (submittedAt < testRegisteredAt) return -1
            return Duration.ofMillis(submittedAt - testRegisteredAt).toHours().toInt()
        }

    val ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int
        get() = storage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value

    val ewHoursSinceHighRiskWarningAtTestRegistration: Int
        get() = storage.ewHoursSinceHighRiskWarningAtTestRegistration.value

    val ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration: Int
        get() = storage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.value

    val ptHoursSinceHighRiskWarningAtTestRegistration: Int
        get() = storage.ptHoursSinceHighRiskWarningAtTestRegistration.value

    val submittedWithCheckIns: Boolean
        get() = storage.submittedWithCheckIns.value

    fun reset() = storage.clear()
}
