package de.rki.coronawarnapp.eventregistration.checkins.split

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

private val DAY_IN_SECONDS = TimeUnit.DAYS.toSeconds(1)

private fun toMidnightUTC(timestampSeconds: Long) = (timestampSeconds / DAY_IN_SECONDS) * DAY_IN_SECONDS

private fun daysInSeconds(days: Long) = TimeUnit.DAYS.toSeconds(days)

/**
 * Splits a [CheckIn] by midnight UTC time into multiple [CheckIn]s across multiple days.
 *
 * @return [List] of [CheckIn]s
 */
fun CheckIn.splitByMidnightUTC(): List<CheckIn> {
    val startTimestampInSeconds = checkInStart.seconds
    val endTimestampInSeconds = checkInEnd?.seconds ?: 0L

    val durationInSeconds = endTimestampInSeconds - startTimestampInSeconds
    Timber.i("durationInSeconds: $durationInSeconds")

    val durationInDays = max(1, ceil(durationInSeconds.toDouble()).toLong())
    Timber.i("durationInDays: $durationInDays")

    fun isFirstDay(day: Long): Boolean {
        return day == 0L
    }

    fun isLastDay(day: Long): Boolean {
        return day == durationInDays - 1
    }

    val checkIns = mutableListOf<CheckIn>()
    for (day in 0 until durationInDays) {
        val checkInCopy = when {
            isFirstDay(day) && !isLastDay(day) -> {
                val checkInEnd = toMidnightUTC(startTimestampInSeconds) + daysInSeconds(day + 1)
                copy(checkInEnd = Instant.ofEpochSecond(checkInEnd))
            }

            !isFirstDay(day) && isLastDay(day) -> {
                copy(checkInStart = Instant.ofEpochSecond(toMidnightUTC(endTimestampInSeconds)))
            }

            !isFirstDay(day) && !isLastDay(day) -> {
                val startSeconds = toMidnightUTC(startTimestampInSeconds) + daysInSeconds(day)
                val endSeconds = toMidnightUTC(startTimestampInSeconds) + daysInSeconds(day + 1)
                copy(
                    checkInStart = Instant.ofEpochSecond(startSeconds),
                    checkInEnd = Instant.ofEpochSecond(endSeconds)
                )
            }
            else -> {
                copy()
            }
        }

        checkIns.add(checkInCopy)
    }


    return checkIns
}
