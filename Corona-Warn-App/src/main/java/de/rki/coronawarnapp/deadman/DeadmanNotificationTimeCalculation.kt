package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.Hours
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor(
    val timeStamper: TimeStamper,
    val enfClient: ENFClient
) {

    /**
     * Calculate initial delay in minutes for deadman notification
     */
    fun getHoursDiff(lastSuccess: Instant) : Int {
        val hours = Hours.hoursBetween(lastSuccess, timeStamper.nowUTC);
        return (DEADMAN_NOTIFICATION_DELAY - hours.hours) * DateTimeConstants.MINUTES_PER_HOUR
    }

    /**
     * Get initial delay in minutes for deadman notification
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY]
     */
    suspend fun getDelay(): Long {
        val lastSuccess = enfClient.latestFinishedCalculation().first()?.finishedAt
        return if (lastSuccess != null) {
            getHoursDiff(lastSuccess).toLong()
        } else {
            (DEADMAN_NOTIFICATION_DELAY * DateTimeConstants.MINUTES_PER_HOUR).toLong()
        }
    }

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY = 36;
    }
}
