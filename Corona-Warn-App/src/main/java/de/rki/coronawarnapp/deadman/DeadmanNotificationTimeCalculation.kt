package de.rki.coronawarnapp.deadman

import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppInjector
import kotlinx.coroutines.flow.first
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Hours
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor() {

    /**
     * Calculate initial delay in minutes for deadman notification
     *
     * TODO: use timerStamper.notUTC
     */
    fun getHoursDiff(lastSuccess: Instant, currentTime:Instant = Instant.now()) : Int {
        val hours = Hours.hoursBetween(lastSuccess, currentTime);
        return (DEADMAN_NOTIFICATION_DELAY - hours.hours) * DateTimeConstants.MINUTES_PER_HOUR
    }

    /**
     * Get initial delay in minutes for deadman notification
     * If last success date time is null (eg: on application first start) - return [DEADMAN_NOTIFICATION_DELAY]
     */
    suspend fun getDelay(): Long {
        val lastSuccess = AppInjector.component.enfClient.latestFinishedCalculation().first()!!.finishedAt
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
