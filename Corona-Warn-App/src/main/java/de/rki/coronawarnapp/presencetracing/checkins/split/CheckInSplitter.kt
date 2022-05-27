package de.rki.coronawarnapp.presencetracing.checkins.split

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import java.time.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * Splits a [CheckIn] by midnight UTC time into multiple [CheckIn]s across multiple days.
 *
 * @return [List] of [CheckIn]s
 */
fun CheckIn.splitByMidnightUTC(): List<CheckIn> {
    Timber.d("Starting splitByMidnightUTC ...")

    val startTimeSeconds = checkInStart.seconds
    val endTimeSeconds = checkInEnd.seconds
    if (endTimeSeconds < startTimeSeconds) return listOf()
    if (checkInStart.toLocalDateUtc() == checkInEnd.toLocalDateUtc()) return listOf(this)

    val durationSecondsUTC = endTimeSeconds - startTimeSeconds.toMidnightUTC()
    Timber.i("durationSecondsUTC=$durationSecondsUTC")

    val durationDays = ceil(durationSecondsUTC.toDouble() / DAY_IN_SECONDS).toLong()
    Timber.i("durationDays=$durationDays")

    return (0 until durationDays).map { day ->
        checkInCopy(
            day,
            durationDays,
            startTimeSeconds,
            endTimeSeconds
        )
    }.also { it.print() }
}

private fun CheckIn.checkInCopy(
    day: Long,
    durationDays: Long,
    startTimeSeconds: Long,
    endTimeSeconds: Long
): CheckIn = when {
    day.isFirstDay() && !day.isLastDay(durationDays) ->
        copy(
            checkInEnd = toInstant(
                seconds = startTimeSeconds.toMidnightUTC() + daysInSeconds(day + 1)
            )
        )

    !day.isFirstDay() && day.isLastDay(durationDays) ->
        copy(
            checkInStart = toInstant(seconds = endTimeSeconds.toMidnightUTC())
        )

    !day.isFirstDay() && !day.isLastDay(durationDays) ->
        copy(
            checkInStart = toInstant(
                seconds = startTimeSeconds.toMidnightUTC() + daysInSeconds(day)
            ),
            checkInEnd = toInstant(
                seconds = startTimeSeconds.toMidnightUTC() + daysInSeconds(day + 1)
            )
        )

    else -> copy()
}

private fun Long.toMidnightUTC(): Long =
    (this / DAY_IN_SECONDS) * DAY_IN_SECONDS

private fun daysInSeconds(days: Long): Long =
    TimeUnit.DAYS.toSeconds(days)

private fun toInstant(seconds: Long): Instant =
    Instant.ofEpochSecond(seconds)

private fun Long.isFirstDay(): Boolean {
    return this == 0L
}

private fun Long.isLastDay(days: Long): Boolean {
    return this == days - 1L
}

private fun List<CheckIn>.print() = Timber.i(
    "SplitCheckIns=[%s]",
    joinToString(separator = ",\n") { checkIn ->
        "{checkInStart=%s,checkOutEnd=%s}".format(
            checkIn.checkInStart,
            checkIn.checkInEnd
        )
    }
)

private val DAY_IN_SECONDS = TimeUnit.DAYS.toSeconds(1)
