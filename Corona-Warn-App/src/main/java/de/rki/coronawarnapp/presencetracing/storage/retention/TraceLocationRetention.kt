package de.rki.coronawarnapp.presencetracing.storage.retention

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import java.time.Instant
import java.util.concurrent.TimeUnit

private const val TRACE_LOCATION_RETENTION_DAYS = 15
private val TRACE_LOCATION_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(TRACE_LOCATION_RETENTION_DAYS.toLong())

/**
 * returns true if a trace location either has no end date or has an end date that isn't older than
 * [TRACE_LOCATION_RETENTION_DAYS], otherwise false
 */
fun TraceLocation.isWithinRetention(now: Instant): Boolean {
    val retentionThreshold = (now.seconds - TRACE_LOCATION_RETENTION_SECONDS)
    return if (endDate == null) {
        true
    } else {
        endDate.seconds >= retentionThreshold
    }
}

/**
 * Returns true if a trace location is stale and therefore can be deleted, otherwise false
 */
fun TraceLocation.isOutOfRetention(now: Instant) = !isWithinRetention(now)
