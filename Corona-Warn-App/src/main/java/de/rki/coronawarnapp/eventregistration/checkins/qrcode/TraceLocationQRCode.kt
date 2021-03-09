package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass

data class TraceLocationQRCode(
    val traceLocation: EventOuterClass.Event
)
