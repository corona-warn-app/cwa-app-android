package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.storage.LocalData
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration

/**
 * This Calculator class takes multiple parameters to check if the Google API
 * can be called or the Rate Limit has been reached.
 *
 * @property incrementByAmount The amount of Quota Calls to increment per Call
 * @property quotaLimit The maximum amount of Quota Calls allowed before Rate Limiting
 * @property quotaResetPeriod The Period after which the Quota Resets
 * @property quotaTimeZone The Timezone to work in
 * @property quotaChronology The Chronology to work in
 */
class GoogleQuotaCalculator(
    val incrementByAmount: Int,
    val quotaLimit: Int,
    val quotaResetPeriod: Duration,
    val quotaTimeZone: DateTimeZone,
    val quotaChronology: Chronology
) : QuotaCalculator<Int> {
    override var isAboveQuota: Boolean = false

    override fun calculateQuota() {
        val currentDateTime = DateTime
            .now(quotaTimeZone)
            .withChronology(quotaChronology)

        if (currentDateTime.isAfter(LocalData.nextTimeRateLimitingUnlocks)) {
            LocalData.nextTimeRateLimitingUnlocks = DateTime
                .now(quotaTimeZone)
                .withChronology(quotaChronology)
                .plus(quotaResetPeriod)
                .withTimeAtStartOfDay()
                .millis
            LocalData.googleAPIProvideDiagnosisKeysCallCount = 0
        }

        if (LocalData.googleAPIProvideDiagnosisKeysCallCount <= quotaLimit) {
            LocalData.googleAPIProvideDiagnosisKeysCallCount += incrementByAmount
        }

        isAboveQuota = LocalData.googleAPIProvideDiagnosisKeysCallCount > quotaLimit
    }

    override fun resetProgressTowardsQuota(newProgress: Int) {
        if (newProgress > quotaLimit) {
            throw IllegalArgumentException("cannot reset progress to a value higher than the quota limit")
        }
        LocalData.googleAPIProvideDiagnosisKeysCallCount = newProgress
        isAboveQuota = false
    }

    override fun getProgressTowardsQuota(): Int = LocalData.googleAPIProvideDiagnosisKeysCallCount
}
