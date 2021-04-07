package de.rki.coronawarnapp.eventregistration.storage.retention

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

private const val TRACE_LOCATION_RETENTION_DAYS = 15
private val TRACE_LOCATION_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(TRACE_LOCATION_RETENTION_DAYS.toLong())

fun TraceLocation.isWithinRetention(now: Instant): Boolean {
    val retentionThreshold = (now.seconds - TRACE_LOCATION_RETENTION_SECONDS)
    return if (endDate == null) {
        true
    } else {
        endDate.seconds >= retentionThreshold
    }
}

fun TraceLocation.isOutOfRetention(now: Instant) = !isWithinRetention(now)
