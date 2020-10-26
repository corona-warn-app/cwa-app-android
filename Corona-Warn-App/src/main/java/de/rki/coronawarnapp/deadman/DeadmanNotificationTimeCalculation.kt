package de.rki.coronawarnapp.deadman

import dagger.Reusable
import kotlinx.coroutines.delay
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Hours
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor() {

    /**
     * Get normalised initial delay for deadman notification
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY]
     */
    val delay: (DateTime?) -> Long = { lastSuccess ->
        if (lastSuccess != null) {
            normaliseInitialDelay(getTime(lastSuccess)).toLong()
        } else {
            (DEADMAN_NOTIFICATION_DELAY * DateTimeConstants.MINUTES_PER_HOUR).toLong()
        }
    }

    /**
     * Calculate initial delay for deadman notification
     */
    fun getTime(lastSuccess:DateTime, currentDate: DateTime = DateTime(Instant.now(), DateTimeZone.UTC)): Int {
        // Calculate hours between two dates
        var hours = Hours.hoursBetween(lastSuccess, currentDate)

        // Calculate initial delay for the notification
        return (DEADMAN_NOTIFICATION_DELAY - hours.hours) * DateTimeConstants.MINUTES_PER_HOUR
    }

    /**
     * Normalise initial delay so it would be between [MINIMUM_DELAY] and [DEADMAN_NOTIFICATION_DELAY] in minutes
     */
    val normaliseInitialDelay: (Int) -> Int = { delay ->
        delay.coerceIn(MINIMUM_DELAY, DEADMAN_NOTIFICATION_DELAY * DateTimeConstants.MINUTES_PER_HOUR)}

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY = 36;
        /**
         * Deadman notification minimum initial delay
         */
        const val MINIMUM_DELAY = 0;
    }
}
