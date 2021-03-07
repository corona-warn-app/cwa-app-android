package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass

data class EventQRCode(
    val event: EventOuterClass.Event
)
