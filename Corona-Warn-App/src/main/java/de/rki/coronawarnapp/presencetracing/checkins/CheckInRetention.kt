package de.rki.coronawarnapp.presencetracing.checkins

import java.time.Instant
import java.util.concurrent.TimeUnit

private const val CHECK_IN_RETENTION_DAYS = 15
private val CHECK_IN_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(CHECK_IN_RETENTION_DAYS.toLong())

/**
 * returns true if the end date of the check-in isn't older than [CHECK_IN_RETENTION_DAYS], otherwise false
 */
fun CheckIn.isWithinRetention(now: Instant): Boolean {
    val retentionThreshold = (now.epochSecond - CHECK_IN_RETENTION_SECONDS)
    return checkInEnd.epochSecond >= retentionThreshold
}

/**
 * Returns true if a check-in is stale and therefore can be deleted, otherwise false
 */
fun CheckIn.isOutOfRetention(now: Instant) = !isWithinRetention(now)
