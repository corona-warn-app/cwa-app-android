package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.storage.LocalData
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber

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
        val oldQuota = LocalData.googleAPIProvideDiagnosisKeysCallCount
        var currentQuota = oldQuota

        val now = Instant.now()
        val nextUnlock = LocalData.nextTimeRateLimitingUnlocks

        Timber.v(
            "calculateQuota() start! (currentQuota=%s, timeNow=%s, timeReset=%s)",
            oldQuota, now, nextUnlock
        )
        if (now.isAfter(nextUnlock)) {
            LocalData.nextTimeRateLimitingUnlocks = DateTime
                .now(quotaTimeZone)
                .withChronology(quotaChronology)
                .plus(quotaResetPeriod)
                .withTimeAtStartOfDay()
                .toInstant()
            Timber.d("calculateQuota() quota reset to 0.")
            currentQuota = 0
        } else {
            Timber.v("calculateQuota() can't be reset yet.")
        }

        if (currentQuota <= quotaLimit) {
            currentQuota += incrementByAmount
        }

        if (currentQuota != oldQuota) {
            LocalData.googleAPIProvideDiagnosisKeysCallCount = currentQuota
        }

        return (currentQuota > quotaLimit).also {
            hasExceededQuota = it
            Timber.v(
                "calculateQuota() done! -> oldQuota=%d, currentQuotaHm=%d, quotaLimit=%d, EXCEEDED=%b",
                oldQuota, currentQuota, quotaLimit, it
            )
        }
    }

    override fun resetProgressTowardsQuota(newProgress: Int) {
        if (newProgress > quotaLimit) {
            Timber.w("cannot reset progress to a value higher than the quota limit")
            return
        }
        if (newProgress % incrementByAmount != 0) {
            Timber.e("supplied progress is no multiple of $incrementByAmount")
            return
        }
        LocalData.googleAPIProvideDiagnosisKeysCallCount = newProgress
        hasExceededQuota = false
        Timber.d("resetProgressTowardsQuota(newProgress=%d) done", newProgress)
    }

    override fun getProgressTowardsQuota(): Int = LocalData.googleAPIProvideDiagnosisKeysCallCount
}
