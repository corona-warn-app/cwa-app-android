package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor() : QRCodeVerifier {
    override suspend fun verify(encodedEvent: String): EventQRCode {
        // TODO Implement verification
        //  For now just parse
        return EventQRCode(
            event = EventOuterClass.Event.parseFrom(encodedEvent.decodeBase32().toByteArray())
        )
    }
}
