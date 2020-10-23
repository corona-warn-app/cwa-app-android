package de.rki.coronawarnapp.deadman

import dagger.Reusable
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.Hours
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DeadmanNotificationTimeCalculation @Inject constructor() {

    fun getTime(lastSuccess:DateTime, currentDate: DateTime = DateTime(Instant.now(), DateTimeZone.UTC)): Int {
        // Calculate hours between two dates
        var hours = Hours.hoursBetween(lastSuccess, currentDate)

        // Calculate initial delay for the notification
        return (DEADMAN_NOTIFICATION_DELAY - hours.hours) * DateTimeConstants.MINUTES_PER_HOUR
    }

    companion object {
        /**
         * Deadman notification background job delay set to 36 hours
         */
        const val DEADMAN_NOTIFICATION_DELAY = 36;
    }
}
