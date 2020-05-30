package de.rki.coronawarnapp.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.chrono.GJChronology
import org.joda.time.format.DateTimeFormat
import java.util.Date
import java.util.concurrent.TimeUnit

object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)
    private const val MS_TO_HOURS = (1000 * 60 * 60)
    private const val MS_TO_SECONDS = 1000

    fun getCurrentHourUTC(): Int = DateTime(Instant.now(), DateTimeZone.UTC).hourOfDay().get()

    fun Date.getHourFromUTCDate(): Int = DateTime(this, DateTimeZone.UTC).hourOfDay().get()

    fun String.toMillis(): Long? = DateTime.parse(this).millis

    fun Date.toServerFormat(): String =
        DateTimeFormat.forPattern("yyyy-MM-dd").withChronology(GJChronology.getInstance())
            .withZoneUTC()
            .print(this.time)

    fun Date.logUTCFormat(): String = DateTime(this, DateTimeZone.UTC).toString()

    fun Long.millisecondsToSeconds(): Long {
        return this.div(MS_TO_SECONDS)
    }

    fun Long.millisecondsToDays(): Long {
        return this.div(MS_TO_DAYS)
    }

    fun Long.millisecondsToHours(): Long {
        return this.div(MS_TO_HOURS)
    }

    fun Long.daysToMilliseconds(): Long {
        return this.times(MS_TO_DAYS)
    }

    /**
     * Converts milliseconds to human readable format hh:mm:ss
     *
     * @return String
     *
     * @see TimeUnit
     */
    fun Long.millisecondsToHMS() = String.format(
        "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(this) % TimeUnit.MINUTES.toSeconds(1)
    )
}
