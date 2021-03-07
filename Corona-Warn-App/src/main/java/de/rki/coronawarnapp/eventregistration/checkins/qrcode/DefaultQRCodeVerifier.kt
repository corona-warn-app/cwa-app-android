package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import timber.log.Timber
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor() : QRCodeVerifier {
    override suspend fun verify(encodedEvent: String): EventQRCode {
        Timber.i("encodedEvent: $encodedEvent")
        // TODO Implement verification
        //  For now just parse
        return EventQRCode(
            event = SignedEventOuterClass.SignedEvent.parseFrom(encodedEvent.decodeBase32().toByteArray()).event
        )
    }
}
