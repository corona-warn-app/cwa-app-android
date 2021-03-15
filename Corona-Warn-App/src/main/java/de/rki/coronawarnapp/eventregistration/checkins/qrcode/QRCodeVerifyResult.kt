package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant

data class QRCodeVerifyResult(
    val singedTraceLocation: TraceLocationOuterClass.SignedTraceLocation,
    val traceLocation: TraceLocationOuterClass.TraceLocation
) {
    fun isBeforeStartTime(now: Instant): Boolean {
        val startTimestamp = traceLocation.startTimestamp
        return startTimestamp != 0L && startTimestamp > now.seconds
    }

    fun isAfterEndTime(now: Instant): Boolean {
        val endTimestamp = traceLocation.endTimestamp
        return endTimestamp != 0L && endTimestamp < now.seconds
    }
}
