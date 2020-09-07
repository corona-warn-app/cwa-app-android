package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.storage.LocalData
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant

/**
 * This Calculator class takes multiple parameters to check if the Google API
 * can be called or the Rate Limit has been reached. The Quota is expected to reset at
 * the start of the day in the given timeZone and Chronology
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
    override var hasExceededQuota: Boolean = false

    override fun calculateQuota(): Boolean {
        if (Instant.now().isAfter(LocalData.nextTimeRateLimitingUnlocks)) {
            LocalData.nextTimeRateLimitingUnlocks = DateTime
                .now(quotaTimeZone)
                .withChronology(quotaChronology)
                .plus(quotaResetPeriod)
                .withTimeAtStartOfDay()
                .toInstant()
            LocalData.googleAPIProvideDiagnosisKeysCallCount = 0
        }

        if (LocalData.googleAPIProvideDiagnosisKeysCallCount <= quotaLimit) {
            LocalData.googleAPIProvideDiagnosisKeysCallCount += incrementByAmount
        }

        hasExceededQuota = LocalData.googleAPIProvideDiagnosisKeysCallCount > quotaLimit

        return hasExceededQuota
    }

    override fun resetProgressTowardsQuota(newProgress: Int) {
        if (newProgress > quotaLimit) {
            throw IllegalArgumentException("cannot reset progress to a value higher than the quota limit")
        }
        if (newProgress % incrementByAmount != 0) {
            throw IllegalArgumentException("supplied progress is no multiple of $incrementByAmount")
        }
        LocalData.googleAPIProvideDiagnosisKeysCallCount = newProgress
        hasExceededQuota = false
    }

    override fun getProgressTowardsQuota(): Int = LocalData.googleAPIProvideDiagnosisKeysCallCount
}
