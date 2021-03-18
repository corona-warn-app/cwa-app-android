package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

data class TraceLocationVerifyResult(
    val signedTraceLocation: TraceLocationOuterClass.SignedTraceLocation,
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

    val verifiedTraceLocation: TraceLocation = TraceLocation(
        guid = traceLocation.guid,
        version = traceLocation.version,
        type = traceLocation.type,
        description = traceLocation.description,
        address = traceLocation.address,
        startDate = traceLocation.startTimestamp.toInstant(),
        endDate = traceLocation.endTimestamp.toInstant(),
        defaultCheckInLengthInMinutes = traceLocation.defaultCheckInLengthInMinutes,
        signature = signedTraceLocation.signature.toByteArray().toByteString()
    )

    /**
     * Converts time in seconds into [Instant]
     */
    private fun Long.toInstant() =
        if (this == 0L) null else Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(this))
}
