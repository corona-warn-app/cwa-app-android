package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

class DefaultQRCodeVerifier @Inject constructor() : QRCodeVerifier {
    override suspend fun verify(uri: String): TraceLocationQRCode {
        if (!uri.isValidQRCodeUri()) {
            throw IllegalArgumentException("Invalid QRCode Uri:$uri")
        }
        val encodedTraceLocation = uri.substringAfterLast("/")
        Timber.i("encodedTraceLocation: $encodedTraceLocation")
        // TODO Implement verification
        //  For now just parse
        return TraceLocationQRCode(
            traceLocation = SignedEventOuterClass.SignedEvent
                .parseFrom(
                    encodedTraceLocation.decodeBase32().toByteArray()
                ).event
        )
    }
}
