package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Duration
import javax.inject.Inject
import kotlin.math.max

class AnalyticsKeySubmissionRepository @Inject constructor(
    private val storage: AnalyticsKeySubmissionStorage,
    private val timeStamper: TimeStamper
) {
    val testResultReceivedAt
        get() = storage.testResultReceivedAt.value

    val testRegisteredAt
    //get() = storage.testResultReceivedAt.value

    val submitted
        get() = storage.submitted.value

    val submittedInBackground
        get() = storage.submittedInBackground.value

    val submittedAfterCancel
        get() = storage.submittedAfterCancel.value

    val submittedAfterSymptomFlow
        get() = storage.submittedAfterSymptomFlow.value

    val submittedWithTeleTAN
        get() = storage.submittedWithTeleTAN.value

    val lastSubmissionFlowScreen
        get() = storage.lastSubmissionFlowScreen.value

    val advancedConsentGiven
        get() = storage.advancedConsentGiven.value

    val hoursSinceTestResult: Int
        get() = Duration.millis(max(timeStamper.nowUTC.millis - testResultReceivedAt, 0)).toStandardHours().hours

    val hoursSinceTestRegistration
        get() = Duration.millis(max(timeStamper.nowUTC.millis - testRegisteredAt, 0)).toStandardHours().hours

    //////
    val hoursSinceHighRiskWarningAtTestRegistration
        get() = storage.hoursSinceHighRiskWarningAtTestRegistration.value

    val daysSinceMostRecentDateAtRiskLevelAtTestRegistration
        get() = storage.daysSinceMostRecentDateAtRiskLevelAtTestRegistration.value

    fun reset() = storage.clear()
}
