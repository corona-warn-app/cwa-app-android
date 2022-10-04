package de.rki.coronawarnapp.util

import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)

    fun Date.toUIFormat(context: Context): String = DateFormat.getDateFormat(context).format(this)

    fun Long.daysToMilliseconds(): Long = this.times(MS_TO_DAYS)

    /**
     * Converts a Long to Instant or null if the long is 0 or null
     */
    fun Long?.toInstantOrNull(): Instant? =
        if (this != null && this != 0L) {
            Instant.ofEpochMilli(this)
        } else null

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun Instant.derive10MinutesInterval(): Long = epochSecond / TimeUnit.MINUTES.toSeconds(10)

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun Instant.deriveHourInterval(): HourInterval = toEpochMilli() / 3600000

    fun Instant.toDate(): Date = Date.from(this)
}

typealias HourInterval = Long
