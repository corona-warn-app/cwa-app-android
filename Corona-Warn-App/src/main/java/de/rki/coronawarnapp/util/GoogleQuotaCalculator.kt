package de.rki.coronawarnapp.util

import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import timber.log.Timber

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
) : QuotaCalculator {

    /**
     * Initially null, the next time rate limiting occurs is initialized after the first call
     * of isAboveQuota()
     */
    private var nextTimeRateLimitingUnlocks: DateTime? = null

    /**
     * The current amount of API Calls made
     */
    private var amountOfAPICalls = 0

    override fun isAboveQuota(): Boolean {
        val currentDateTime = currentDateTime()
        Timber.v("current for quota check is $currentDateTime")

        // initialize nextTimeRateLimitingUnlocks or check on Epoch value
        if (nextTimeRateLimitingUnlocks == null) {
            nextTimeRateLimitingUnlocks = newRateLimitUnlockDateTime()
        } else if (currentDateTime.isAfter(nextTimeRateLimitingUnlocks)) {
            nextTimeRateLimitingUnlocks = newRateLimitUnlockDateTime()
            amountOfAPICalls = 0
        }

        Timber.v("next time rate limiting unlocks: $nextTimeRateLimitingUnlocks")

        // if not already above the quota limit, increase the api call count by increment amount
        if (amountOfAPICalls <= quotaLimit) {
            amountOfAPICalls += incrementByAmount
        }

        val isAboveQuotaLimit = amountOfAPICalls > quotaLimit
        Timber.v("Amount of API Calls: $amountOfAPICalls, Quota Limit: $quotaLimit, Is above Limit: $isAboveQuotaLimit")
        return amountOfAPICalls > quotaLimit
    }

    private fun currentDateTime(): DateTime = DateTime
        .now(quotaTimeZone)
        .withChronology(quotaChronology)

    /**
     * Calculate the new Rate Limiting Unlock Time based on the assumption
     * that it is always at the start of a day based on the given TimeZone
     *
     * @return unlock DateTime
     */
    private fun newRateLimitUnlockDateTime(): DateTime = currentDateTime()
        .plus(quotaResetPeriod)
        .withTimeAtStartOfDay()
}
