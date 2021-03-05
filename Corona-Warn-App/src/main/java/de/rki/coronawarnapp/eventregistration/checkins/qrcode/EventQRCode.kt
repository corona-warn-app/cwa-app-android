package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass

@Suppress("EmptyClassBlock")
interface EventQRCode {
    val event: EventOuterClass.Event
}
