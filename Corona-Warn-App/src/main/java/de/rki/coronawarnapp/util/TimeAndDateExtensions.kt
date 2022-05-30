package de.rki.coronawarnapp.util

import android.content.Context
import android.text.format.DateFormat
import com.google.common.math.DoubleMath.roundToLong
import timber.log.Timber
import java.math.RoundingMode
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.DateFormat.getDateInstance
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)
    private const val MS_TO_HOURS = (1000 * 60 * 60)
    private const val MS_TO_SECONDS = 1000

    // TODO: check all formats
    private val dayFormatter = getDateInstance(MEDIUM)
    private val dayFormatter2DigitYear = DateTimeFormatter.ofPattern("yy-MM-dd")
    private val serverDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val shortTime = java.text.DateFormat.getDateInstance(SHORT)

    fun Date.toUIFormat(context: Context): String = DateFormat.getDateFormat(context).format(this)

    fun Date.logUTCFormat(): String = shortTime.format(this)

    fun Long.millisecondsToSeconds(): Long = this.div(MS_TO_SECONDS)

    fun Long.millisecondsToHours(): Long = this.div(MS_TO_HOURS)

    fun Long.daysToMilliseconds(): Long = this.times(MS_TO_DAYS)

    fun Instant.toDate():Date = Date.from(this)

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
    fun Instant.deriveHourInterval(): HourInterval = toEpochMilli() / 3600000

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

    fun LocalDate.toDate(): Date = Date(this.year, this.monthValue, this.dayOfYear)

    fun LocalDateTime.toDate(): Date = Date(this.year, this.monthValue, this.dayOfYear)

    fun LocalDate.ageInDays(now: LocalDate) = ChronoUnit.DAYS.between(this, now)

    fun Instant.toLocalDateUtc(): LocalDate = atZone(ZoneOffset.UTC).toLocalDate()

    fun Instant.toDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(this, zoneId)

    fun Instant.toLocalDateTime(timeZone: ZoneId): LocalDateTime =
        LocalDateTime.ofInstant(this, timeZone)

    fun Instant.toLocalTimeUtc(): LocalTime = this.toDateTime(ZoneOffset.UTC).toLocalTime()

    val Instant.seconds get() = TimeUnit.MILLISECONDS.toSeconds(toEpochMilli())

    fun Instant.toUserTimeZone(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneId.systemDefault())

    fun Instant.toLocalDateUserTz(): LocalDate = this.toUserTimeZone().toLocalDate()

    fun Instant.toLocalDateTimeUserTz(): LocalDateTime = this.toUserTimeZone().toLocalDateTime()

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of an Instant
     */
//    fun Instant.toDayFormat(): String = toString(dayFormatter)

    fun LocalDate.toInstantMidnightUtc() =
        LocalDateTime.of(this, LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC)

    fun LocalDate.toLocalDateTime(localTime: LocalTime) =
        LocalDateTime.of(this, localTime)

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a LocalDate
     */
    fun LocalDate.toDayFormat(): String = dayFormatter.format(this)

    fun OffsetDateTime.toDayFormat(): String = dayFormatter.format(this)

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a DateTime
     */
//    fun DateTime.toDayFormat(): String = toString(dayFormatter)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of a DateTime
     */
//    fun DateTime.toShortDayFormat(): String = toString(dayFormatter2DigitYear)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of a LocalDateTime
     */
    fun LocalDateTime.toShortDayFormat(): String = dayFormatter2DigitYear.format(this)

    /**
     * Returns a readable date String with the format "dd.MM.yyyy" like 23.05.1989 of a LocalDateTime
     */
    fun LocalDateTime.toDayFormat(): String = dayFormatter.format(this)

    /**
     * Returns a readable date String with the format "dd.MM.yy hh:mm" like 23.05.89 12:00 of a DateTime
     */
//    fun DateTime.toShortDateTimeFormat(): String = toString(DateTimeFormat.shortDateTime())

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a LocalDate
     */
    fun LocalDate.toShortTimeFormat(): String = shortTime.format(this)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a Instant
     */
    fun Instant.toShortTimeFormat(): String = shortTime.format(this)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a DateTime
     */
//    fun DateTime.toShortTimeFormat(): String = toString(shortTime)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a DateTime
     */
    fun LocalTime.toShortTimeFormat(): String = shortTime.format(this)

    /**
     * Returns a readable time String with the format "hh:mm" like 12:00 of a LocalDateTime
     */
    fun LocalDateTime.toShortTimeFormat(): String = shortTime.format(this)

    fun OffsetDateTime.toShortTimeFormat(): String = shortTime.format(this)

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of an Instant
     */
    fun Instant.toShortDayFormat(): String = dayFormatter2DigitYear.format(toUserTimeZone())

    /**
     * Returns a readable date String with the format "dd.MM.yy" like 23.05.89 of an LocalDate
     */
    fun LocalDate.toShortDayFormat(): String = dayFormatter2DigitYear.format(this)

    fun OffsetDateTime.toShortDayFormat(): String = dayFormatter2DigitYear.format(this)

    /**
     * Converts this LocalDate to a full datetime at the earliest valid time for the date using timezone UTC
     */
    fun LocalDate.toDateTimeAtStartOfDayUtc(): OffsetDateTime = toDateTimeAtStartOfDay(ZoneOffset.UTC)

    fun LocalDate.toDateTimeAtStartOfDay(offset: ZoneOffset = ZoneOffset.UTC): OffsetDateTime =
        OffsetDateTime.of(this, LocalTime.MIN, offset)

    fun LocalDate.toDateTime(localTime: LocalTime?, offset: ZoneOffset = ZoneOffset.UTC): OffsetDateTime =
        OffsetDateTime.of(this, localTime ?: LocalTime.MIN, offset)

    fun OffsetDateTime.toDateTime(offset: ZoneOffset): OffsetDateTime =
        OffsetDateTime.of(toLocalDateTime(), offset)

    /*
    * Returns date changes until
    */
    fun Instant.daysUntil(
        date: Instant,
        timeZone: ZoneId = ZoneId.systemDefault()
    ): Int {
        val startDate = toDateTime(timeZone).toLocalDate()
        val endDate = date.toDateTime(timeZone).toLocalDate()
        return ChronoUnit.DAYS.between(startDate, endDate).toInt()
    }
}

typealias HourInterval = Long
