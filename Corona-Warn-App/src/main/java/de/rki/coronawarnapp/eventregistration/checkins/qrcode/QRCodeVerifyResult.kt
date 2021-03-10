package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import org.joda.time.Instant

data class QRCodeVerifyResult(
    val singedTraceLocation: SignedEventOuterClass.SignedEvent
) {
    fun isBeforeStartTime(now: Instant): Boolean =
        singedTraceLocation.event.start != 0 && singedTraceLocation.event.start > now.seconds

    fun isAfterEndTime(now: Instant): Boolean =
        singedTraceLocation.event.end != 0 && singedTraceLocation.event.end < now.seconds
}
