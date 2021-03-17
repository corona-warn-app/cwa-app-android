package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

/**
 * Splits a [CheckIn] by midnight UTC time into multiple [CheckIn]s across multiple days.
 *
 * @return [List] of [CheckIn]s
 */
fun CheckIn.splitByMidnightUTC(): List<CheckIn> {
    val startTimeSeconds = checkInStart.seconds
    val endTimeSeconds = checkInEnd?.seconds ?: 0L
    val durationSeconds = endTimeSeconds - startTimeSeconds

    Timber.i("durationSeconds=$durationSeconds")

    val durationDays = max(1L, ceil(durationSeconds.toDouble() / DAY_IN_SECONDS).toLong())
    Timber.i("durationDays=$durationDays")

    return Array(durationDays.toInt()) { day ->
        checkInCopy(
            day.toLong(),
            durationDays,
            startTimeSeconds,
            endTimeSeconds
        )
    }.toList().also { it.print() }
}

private fun CheckIn.checkInCopy(
    day: Long,
    durationDays: Long,
    startTimeSeconds: Long,
    endTimeSeconds: Long
): CheckIn = when {
    isFirstDay(day) && !isLastDay(day, durationDays) ->
        copy(
            checkInEnd = toInstant(
                seconds = toMidnightUTC(startTimeSeconds) + daysInSeconds(day + 1)
            )
        )

    !isFirstDay(day) && isLastDay(day, durationDays) ->
        copy(
            checkInStart = toInstant(seconds = toMidnightUTC(endTimeSeconds))
        )

    !isFirstDay(day) && !isLastDay(day, durationDays) ->
        copy(
            checkInStart = toInstant(
                seconds = toMidnightUTC(startTimeSeconds) + daysInSeconds(day)
            ),
            checkInEnd = toInstant(
                seconds = toMidnightUTC(startTimeSeconds) + daysInSeconds(day + 1)
            )
        )

    else -> copy()
}

private fun toMidnightUTC(timestampSeconds: Long): Long =
    (timestampSeconds / DAY_IN_SECONDS) * DAY_IN_SECONDS

private fun daysInSeconds(days: Long): Long =
    TimeUnit.DAYS.toSeconds(days)

private fun toInstant(seconds: Long): Instant =
    Instant.ofEpochSecond(seconds)

private fun isFirstDay(day: Long): Boolean {
    return day == 0L
}

private fun isLastDay(day: Long, days: Long): Boolean {
    return day == days - 1L
}

private fun List<CheckIn>.print() = Timber.i(
    "splitInto: %s",
    joinToString(separator = "\n") { checkIn ->
        "{checkInStart=%s,checkOutEnd=%s}".format(
            checkIn.checkInStart,
            checkIn.checkInEnd
        )
    }
)

private val DAY_IN_SECONDS = TimeUnit.DAYS.toSeconds(1)
