package de.rki.coronawarnapp.eventregistration.storage.retention

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import java.util.concurrent.TimeUnit

private const val TRACE_LOCATION_RETENTION_DAYS = 15
private val TRACE_LOCATION_RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(TRACE_LOCATION_RETENTION_DAYS.toLong())

fun isWithinRetention(traceLocation: TraceLocation, timeStamper: TimeStamper): Boolean {
    val retentionThreshold = (timeStamper.nowUTC.seconds - TRACE_LOCATION_RETENTION_SECONDS)
    return if (traceLocation.endDate == null) {
        true
    } else {
        traceLocation.endDate.seconds >= retentionThreshold
    }
}

fun isOutOfRetention(traceLocation: TraceLocation, timeStamper: TimeStamper) =
    !isWithinRetention(traceLocation, timeStamper)
