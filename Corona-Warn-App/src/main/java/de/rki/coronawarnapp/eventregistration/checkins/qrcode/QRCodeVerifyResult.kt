package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

data class QRCodeVerifyResult(
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

    val verifiedTraceLocation: VerifiedTraceLocation = with(traceLocation) {
        VerifiedTraceLocation(
            guid = guid.toByteArray().toByteString().base64(),
            version = version,
            type = type,
            description = description,
            address = address,
            start = startTimestamp.toInstant(),
            end = endTimestamp.toInstant(),
            defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes
        )
    }

    /**
     * Converts time in seconds into [Instant]
     */
    private fun Long.toInstant() =
        if (this == 0L) null else Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(this))
}
