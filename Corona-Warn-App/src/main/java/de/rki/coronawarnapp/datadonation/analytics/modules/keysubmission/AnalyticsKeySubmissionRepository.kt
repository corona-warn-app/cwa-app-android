package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import kotlinx.coroutines.flow.first
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

    suspend fun testResultReceivedAt(): Long = storage.testResultReceivedAt.first()

    suspend fun testRegisteredAt(): Long = storage.testRegisteredAt.first()

    suspend fun submitted(): Boolean = storage.submitted.first()

    suspend fun submittedAt(): Long = storage.submittedAt.first()

    suspend fun submittedInBackground(): Boolean = submitted() && storage.submittedInBackground.first()

    suspend fun submittedAfterCancel(): Boolean = submitted() && storage.submittedAfterCancel.first()

    suspend fun submittedAfterSymptomFlow(): Boolean = submitted() && storage.submittedAfterSymptomFlow.first()

    suspend fun submittedWithTeleTAN(): Boolean = submitted() && storage.registeredWithTeleTAN.first()

    suspend fun lastSubmissionFlowScreen(): Int = storage.lastSubmissionFlowScreen.first()

    suspend fun advancedConsentGiven(): Boolean = storage.advancedConsentGiven.first()

    suspend fun hoursSinceTestResult(): Int {
        if (submittedAt() <= 0) return -1
        if (testResultReceivedAt() <= 0) return -1
        if (submittedAt() < testResultReceivedAt()) return -1
        return Duration.ofMillis(submittedAt() - testResultReceivedAt()).toHours().toInt()
    }

    suspend fun hoursSinceTestRegistration(): Int {
        if (submittedAt() <= 0) return -1
        if (testRegisteredAt() <= 0) return -1
        if (submittedAt() < testRegisteredAt()) return -1
        return Duration.ofMillis(submittedAt() - testRegisteredAt()).toHours().toInt()
    }

    suspend fun ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(): Int =
        storage.ewDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first()

    suspend fun ewHoursSinceHighRiskWarningAtTestRegistration(): Int =
        storage.ewHoursSinceHighRiskWarningAtTestRegistration.first()

    suspend fun ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(): Int =
        storage.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration.first()

    suspend fun ptHoursSinceHighRiskWarningAtTestRegistration(): Int =
        storage.ptHoursSinceHighRiskWarningAtTestRegistration.first()

    suspend fun submittedWithCheckIns(): Boolean = storage.submittedWithCheckIns.first()

    suspend fun reset() = storage.clear()
}
