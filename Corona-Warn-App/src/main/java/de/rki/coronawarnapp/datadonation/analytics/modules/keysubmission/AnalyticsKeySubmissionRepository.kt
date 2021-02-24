package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import javax.inject.Inject
import kotlin.math.max

class AnalyticsKeySubmissionRepository @Inject constructor(
    private val storage: AnalyticsKeySubmissionStorage,
    private val timeStamper: TimeStamper,
    private val riskLevelStorage: RiskLevelStorage,
    riskLevelSettings: RiskLevelSettings
) {
    val testResultReceivedAt
        get() = storage.testResultReceivedAt.value

    private val testRegisteredAt: Long
        get() = storage.testRegisteredAt.value

    val submitted
        get() = storage.submitted.value

    val submittedInBackground
        get() = submitted && storage.submittedInBackground.value

    val submittedAfterCancel
        get() = submitted && storage.submittedAfterCancel.value

    val submittedAfterSymptomFlow
        get() = storage.submittedAfterSymptomFlow.value

    val submittedWithTeleTAN
        get() = submitted && storage.registeredWithTeleTAN.value

    val lastSubmissionFlowScreen
        get() = storage.lastSubmissionFlowScreen.value

    val advancedConsentGiven
        get() = submitted && storage.advancedConsentGiven.value

    val hoursSinceTestResult: Int
        get() = Duration.millis(max(timeStamper.nowUTC.millis - testResultReceivedAt, 0)).toStandardHours().hours

    val hoursSinceTestRegistration
        get() = Duration.millis(max(timeStamper.nowUTC.millis - testRegisteredAt, 0L)).toStandardHours().hours

    suspend fun hoursSinceHighRiskWarningAtTestRegistration(): Int {
        val riskLevelAtRegistration = storage.riskLevelAtTestRegistration.value
        return if (riskLevelAtRegistration == PpaData.PPARiskLevel.RISK_LEVEL_LOW) {
            DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING
        } else {
            calculatedHoursSinceHighRiskWarning(Instant.ofEpochMilli(testResultReceivedAt))
        }
    }

    val daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
        Duration(
            riskLevelSettings.lastChangeCheckedRiskLevelTimestamp,
            Instant.ofEpochMilli(testResultReceivedAt)
        ).standardDays.toInt()

    fun reset() = storage.clear()

    private suspend fun calculatedHoursSinceHighRiskWarning(registrationTime: Instant): Int {
        val highRiskResultCalculatedAt = riskLevelStorage
            .latestAndLastSuccessful
            .first()
            .filter { it.isIncreasedRisk }
            .minByOrNull { it.calculatedAt }
            ?.calculatedAt ?: return DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING

        return Duration(
            highRiskResultCalculatedAt,
            registrationTime
        ).standardHours.toInt()
    }
}

private const val DEFAULT_HOURS_SINCE_HIGH_RISK_WARNING = -1
