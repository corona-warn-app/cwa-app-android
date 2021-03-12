package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass

interface QRCodeVerifier {

    /**
     * @param encodedSignedTraceLocation Base32 string
     * representing [TraceLocationOuterClass.SignedTraceLocation]
     */
    suspend fun verify(encodedSignedTraceLocation: String): QRCodeVerifyResult
}
