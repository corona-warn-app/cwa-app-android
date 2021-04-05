package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import okio.ByteString
import okio.ByteString.Companion.toByteString

private const val CWA_GUID = "CWA-GUID"

/**
 *  Returns a byte sequence that serves as an identifier for the trace location.
 *  The ID is the byte representation of SHA-256 hash.
 *
 *  Please note that is an extension value and can not be lazily created here.
 *  it is the responsibility of the consumer to lazily call it to avoid multiple computations
 */
val QRCodePayload.locationId: ByteString
    get() {
        val cwaDomain = CWA_GUID.toByteArray()
        val payloadBytes = toByteArray()
        val totalByteSequence = cwaDomain + payloadBytes
        return totalByteSequence.toByteString().sha256()
    }

/**
 *  Returns a byte sequence that serves as an identifier for the trace location.
 *  The ID is the byte representation of SHA-256 hash.
 *
 *  Please note that is an extension value and can not be lazily created here.
 *  it is the responsibility of the consumer to lazily call it to avoid multiple computations
 */
val TraceLocation.locationId: ByteString get() = qrCodePayload().locationId
