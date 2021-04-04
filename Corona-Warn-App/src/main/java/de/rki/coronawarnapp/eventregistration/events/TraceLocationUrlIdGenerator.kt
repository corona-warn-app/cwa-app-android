package de.rki.coronawarnapp.eventregistration.events

import com.google.common.io.BaseEncoding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CWALocationData
import de.rki.coronawarnapp.ui.eventregistration.organizer.details.QrCodeGenerator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

class TraceLocationUrlIdGenerator @Inject constructor() {

    /**
     * Return a url for [TraceLocation] to be used as an input for [QrCodeGenerator]
     * URL format https://e.coronawarn.app?v=1#QR_CODE_PAYLOAD_BASE64URL
     */
    fun urlForQrCode(traceLocation: TraceLocation): String {
        val payloadBytes = qrCodePayload(traceLocation).toByteArray()
        val base64Url = BaseEncoding.base64Url().omitPadding().encode(payloadBytes)
        return AUTHORITY.plus(base64Url)
    }

    private fun qrCodePayload(traceLocation: TraceLocation): QRCodePayload {
        val vendorData = CWALocationData.newBuilder()
            .setType(traceLocation.type)
            .setDefaultCheckInLengthInMinutes(traceLocation.defaultCheckInLengthInMinutes ?: 0)
            .setVersion(TraceLocation.VERSION)
            .build()

        val crowdNotifierData = TraceLocationOuterClass.CrowdNotifierData.newBuilder()
            .setCryptographicSeed(traceLocation.cryptographicSeed.toProtoByteString())
            .setPublicKey(traceLocation.cnPublicKey.decodeBase64()!!.toProtoByteString())
            .setVersion(TraceLocation.VERSION)

        val locationData = TraceLocationOuterClass.TraceLocation.newBuilder()
            .setDescription(traceLocation.description)
            .setAddress(traceLocation.address)
            .setStartTimestamp(traceLocation.startDate?.seconds ?: 0)
            .setEndTimestamp(traceLocation.endDate?.seconds ?: 0)
            .setVersion(TraceLocation.VERSION)
            .build()

        return QRCodePayload.newBuilder()
            .setVendorData(vendorData.toByteString())
            .setCrowdNotifierData(crowdNotifierData)
            .setLocationData(locationData)
            .setVersion(TraceLocation.VERSION)
            .build()
    }

    companion object {
        private const val AUTHORITY = "https://e.coronawarn.app?v=${TraceLocation.VERSION}#"
    }
}
