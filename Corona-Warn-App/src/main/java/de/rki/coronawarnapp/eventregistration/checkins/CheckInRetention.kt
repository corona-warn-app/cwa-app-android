package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

private const val CHECK_IN_RETENTION_DAYS = 15
private val CHECK_IN_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(CHECK_IN_RETENTION_DAYS.toLong())

fun CheckIn.isWithinRetention(now: Instant): Boolean {
    val retentionThreshold = (now.seconds - CHECK_IN_RETENTION_SECONDS)
    return checkInEnd.seconds >= retentionThreshold
}

fun CheckIn.isOutOfRetention(now: Instant) = !isWithinRetention(now)
