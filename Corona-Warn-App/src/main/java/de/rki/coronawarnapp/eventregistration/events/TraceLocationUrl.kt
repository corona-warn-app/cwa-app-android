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

class TraceLocationUrl @Inject constructor() {

    /**
     * Return a url for [TraceLocation] to be used as an input for [QrCodeGenerator]
     * URL format https://e.coronawarn.app?v=1#QR_CODE_PAYLOAD_BASE64URL
     */
    fun locationUrl(traceLocation: TraceLocation): String {
        val payloadBytes = traceLocation.qrCodePayload().toByteArray()
        val base64Url = BaseEncoding.base64Url().omitPadding().encode(payloadBytes)
        return AUTHORITY.plus(base64Url)
    }

    private fun TraceLocation.qrCodePayload(): QRCodePayload {
        val vendorData = CWALocationData.newBuilder()
            .setType(type)
            .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes ?: 0)
            .setVersion(TraceLocation.VERSION)
            .build()

        val crowdNotifierData = TraceLocationOuterClass.CrowdNotifierData.newBuilder()
            .setCryptographicSeed(cryptographicSeed.toProtoByteString())
            .setPublicKey(cnPublicKey.decodeBase64()!!.toProtoByteString())
            .setVersion(TraceLocation.VERSION)

        val locationData = TraceLocationOuterClass.TraceLocation.newBuilder()
            .setDescription(description)
            .setAddress(address)
            .setStartTimestamp(startDate?.seconds ?: 0)
            .setEndTimestamp(endDate?.seconds ?: 0)
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
