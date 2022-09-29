package de.rki.coronawarnapp.util

import android.content.Context
import android.text.format.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

object TimeAndDateExtensions {

    private const val MS_TO_DAYS = (1000 * 60 * 60 * 24)

    fun Date.toUIFormat(context: Context): String = DateFormat.getDateFormat(context).format(this)

    fun Long.daysToMilliseconds(): Long = this.times(MS_TO_DAYS)

    /**
     * Converts a Long to Instant or null if the long is 0 or null
     */
    fun Long?.toInstantOrNull(): java.time.Instant? =
        if (this != null && this != 0L) {
            java.time.Instant.ofEpochMilli(this)
        } else null

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun java.time.Instant.derive10MinutesInterval(): Long =
        epochSecond / TimeUnit.MINUTES.toSeconds(10) // 10 min in seconds

    /**
     * Derive a UNIX timestamp (in seconds) and returns the corresponding 10-minute interval
     */
    fun java.time.Instant.deriveHourInterval(): HourInterval = toEpochMilli() / 3600000

    fun java.time.Instant.toDate(): Date = Date.from(this)
}

typealias HourInterval = Long
