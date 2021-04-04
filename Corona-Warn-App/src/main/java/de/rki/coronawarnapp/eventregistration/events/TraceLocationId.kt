package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

class TraceLocationId @Inject constructor() {

    /**
     *  Returns a byte sequence that serves as an identifier for the trace location.
     *  The ID is the byte representation of SHA-256 hash.
     *
     *  @param qrCodePayload [QRCodePayload]
     */
    fun locationId(qrCodePayload: QRCodePayload): ByteArray {
        val cwaDomain = CWA_GUID.toByteArray()
        val payloadBytes = qrCodePayload.toByteArray()
        val totalByteSequence = cwaDomain + payloadBytes
        return totalByteSequence.toByteString().sha256().toByteArray()
    }

    companion object {
        private const val CWA_GUID = "CWA-GUID"
    }
}
