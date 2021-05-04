package de.rki.coronawarnapp.util

import android.content.Context
import com.google.common.math.DoubleMath.roundToLong
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.chrono.GJChronology
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.math.RoundingMode
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)
    private const val MS_TO_HOURS = (1000 * 60 * 60)
    private const val MS_TO_SECONDS = 1000

    fun getCurrentHourUTC(): Int = DateTime(Instant.now(), DateTimeZone.UTC).hourOfDay().get()

    fun Date.toServerFormat(): String =
        DateTimeFormat.forPattern("yyyy-MM-dd").withChronology(GJChronology.getInstance())
            .withZoneUTC()
            .print(this.time)

    fun Date.toUIFormat(context: Context): String =
        android.text.format.DateFormat.getDateFormat(context).format(this)

    fun Date.logUTCFormat(): String = DateTime(this, DateTimeZone.UTC).toString()

    fun Long.millisecondsToSeconds(): Long {
        return this.div(MS_TO_SECONDS)
    }

    fun Long.millisecondsToHours(): Long {
        return this.div(MS_TO_HOURS)
    }

    fun Long.daysToMilliseconds(): Long {
        return this.times(MS_TO_DAYS)
    }

    fun Long.roundUpMsToDays(): Long {
        val numberOfDays = this / MS_TO_DAYS.toDouble()
        Timber.v("Number of days traced: $numberOfDays")

        return roundToLong(numberOfDays, RoundingMode.HALF_UP).also {
            Timber.v("Rounded number of days to display: $it")
        }
    }

    /**
     * Converts a Long to Instant or null if the long is 0 or null
     */
    fun Long?.toInstantOrNull(): Instant? =
        if (this != null && this != 0L) {
            Instant.ofEpochMilli(this)
        } else null

    /**
     * Converts a [Long] representing time in Seconds into [Instant]
     */
    fun Long.secondsToInstant(): Instant = Instant.ofEpochSecond(this)

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun Instant.derive10MinutesInterval(): Long =
        seconds / TimeUnit.MINUTES.toSeconds(10) // 10 min in seconds

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun Instant.deriveHourInterval(): HourInterval = millis / 3600000

    /**
     * Converts milliseconds to human readable format hh:mm:ss
     *
     * @return String
     *
     * @see TimeUnit
     */
    fun Long.millisecondsToHMS() = String.format(
        "%02d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(this) % TimeUnit.MINUTES.toSeconds(1)
    )

    fun LocalDate.ageInDays(now: LocalDate) = Days.daysBetween(this, now).days

    fun Instant.toLocalDateUtc(): LocalDate = this.toDateTime(DateTimeZone.UTC).toLocalDate()

    fun Instant.toLocalTime(): LocalTime = this.toDateTime(DateTimeZone.UTC).toLocalTime()

    val Instant.seconds get() = TimeUnit.MILLISECONDS.toSeconds(millis)

    fun Instant.toUserTimeZone() = this.toDateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()))
}

typealias HourInterval = Long
