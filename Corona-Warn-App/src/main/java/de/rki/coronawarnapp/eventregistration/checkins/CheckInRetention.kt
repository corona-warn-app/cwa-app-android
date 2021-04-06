package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import java.util.concurrent.TimeUnit

private const val CHECK_IN_RETENTION_DAYS = 15
private val CHECK_IN_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(CHECK_IN_RETENTION_DAYS.toLong())

fun isWithinRetention(checkIn: CheckIn, timeStamper: TimeStamper): Boolean {
    val retentionThreshold = (timeStamper.nowUTC.seconds - CHECK_IN_RETENTION_SECONDS)
    return checkIn.checkInEnd.seconds >= retentionThreshold
}

fun isOutOfRetention(checkIn: CheckIn, timeStamper: TimeStamper) = !isWithinRetention(checkIn, timeStamper)
