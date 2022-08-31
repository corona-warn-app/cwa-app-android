package de.rki.coronawarnapp.util

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)

    private val dayFormatter = DateTimeFormat.mediumDate()
    private val dayFormatter2DigitYear = DateTimeFormat.shortDate()

    private val shortTime = DateTimeFormat.shortTime()

    fun getCurrentHourUTC(): Int = DateTime(Instant.now(), DateTimeZone.UTC).hourOfDay().get()

    fun Date.toUIFormat(context: Context): String = DateFormat.getDateFormat(context).format(this)

    fun Date.logUTCFormat(): String = DateTime(this, DateTimeZone.UTC).toString()

    fun Long.daysToMilliseconds(): Long = this.times(MS_TO_DAYS)

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

    fun LocalDate.ageInDays(now: LocalDate) = Days.daysBetween(this, now).days

    fun Instant.toLocalDateUtc(): LocalDate = this.toDateTime(DateTimeZone.UTC).toLocalDate()

    fun Instant.toLocalDateTime(timeZone: DateTimeZone): LocalDateTime = this.toDateTime(timeZone).toLocalDateTime()

    val Instant.seconds get() = TimeUnit.MILLISECONDS.toSeconds(millis)

    fun Instant.toUserTimeZone(): DateTime = this.toDateTime(DateTimeZone.getDefault())

    fun Instant.toLocalDateUserTz(): LocalDate = this.toUserTimeZone().toLocalDate()

    fun Instant.toLocalDateTimeUserTz(): LocalDateTime = this.toUserTimeZone().toLocalDateTime()

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of an Instant
     */
    fun Instant.toDayFormat(): String = toString(dayFormatter)

    fun LocalDate.toInstantMidnightUtc() =
        this.toLocalDateTime(LocalTime.MIDNIGHT).toDateTime(DateTimeZone.UTC).toInstant()

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a LocalDate
     */
    fun LocalDate.toDayFormat(): String = toString(dayFormatter)

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a DateTime
     */
    fun DateTime.toDayFormat(): String = toString(dayFormatter)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of a DateTime
     */
    fun DateTime.toShortDayFormat(): String = toString(dayFormatter2DigitYear)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of a LocalDateTime
     */
    fun LocalDateTime.toShortDayFormat(): String = toString(dayFormatter2DigitYear)

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a LocalDateTime
     */
    fun LocalDateTime.toDayFormat(): String = toString(dayFormatter)

    /**
     * Returns a readable date String with the format "dd.MM.yy hh:mm" like 23.05.89 12:00 of a DateTime
     */
    fun DateTime.toShortDateTimeFormat(): String = toString(DateTimeFormat.shortDateTime())

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a LocalDate
     */
    fun LocalDate.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a Instant
     */
    fun Instant.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a DateTime
     */
    fun DateTime.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a DateTime
     */
    fun LocalTime.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a LocalDateTime
     */
    fun LocalDateTime.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of an Instant
     */
    fun Instant.toShortDayFormat(): String = toString(dayFormatter2DigitYear)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of an LocalDate
     */
    fun LocalDate.toShortDayFormat(): String = toString(dayFormatter2DigitYear)

    /**
     * Converts this LocalDate to a full datetime at the earliest valid time for the date using timezone UTC
     */
    fun LocalDate.toDateTimeAtStartOfDayUtc(): DateTime = toDateTimeAtStartOfDay(DateTimeZone.UTC)

    /*
    * Returns date changes until
    */
    fun Instant.daysUntil(
        date: Instant,
        timeZone: DateTimeZone = DateTimeZone.getDefault()
    ): Int {
        val startDate = toDateTime(timeZone).toLocalDate()
        val endDate = date.toDateTime(timeZone).toLocalDate()
        return Days.daysBetween(startDate, endDate).days
    }
}

typealias HourInterval = Long
