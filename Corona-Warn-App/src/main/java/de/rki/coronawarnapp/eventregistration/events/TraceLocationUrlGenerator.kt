package de.rki.coronawarnapp.eventregistration.events

import com.google.common.io.BaseEncoding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TRACE_LOCATION_VERSION
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CWALocationData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

class TraceLocationUrlGenerator @Inject constructor() {

    fun traceLocationUrl(traceLocation: TraceLocation): String {
        val vendorData = CWALocationData.newBuilder()
            .setType(traceLocation.type)
            .setDefaultCheckInLengthInMinutes(traceLocation.defaultCheckInLengthInMinutes ?: 0)
            .setVersion(TRACE_LOCATION_VERSION)
            .build()

        val crowdNotifierData = TraceLocationOuterClass.CrowdNotifierData.newBuilder()
            .setCryptographicSeed(traceLocation.cryptographicSeed.toProtoByteString())
            .setPublicKey(traceLocation.cnPublicKey.decodeBase64()!!.toProtoByteString())
            .setVersion(TRACE_LOCATION_VERSION)

        val locationData = TraceLocationOuterClass.TraceLocation.newBuilder()
            .setDescription(traceLocation.description)
            .setAddress(traceLocation.address)
            .setStartTimestamp(traceLocation.startDate?.seconds ?: 0)
            .setEndTimestamp(traceLocation.endDate?.seconds ?: 0)
            .setVersion(TRACE_LOCATION_VERSION)
            .build()

        val qrCodePayload = QRCodePayload.newBuilder()
            .setVendorData(vendorData.toByteString())
            .setCrowdNotifierData(crowdNotifierData)
            .setLocationData(locationData)
            .setVersion(TRACE_LOCATION_VERSION)
            .build()

        val base64Url = BaseEncoding
            .base64Url()
            .omitPadding()
            .encode(
                qrCodePayload.toByteArray()
            )
        return AUTHORITY.plus(base64Url)
    }

    companion object {
        private const val AUTHORITY = "https://e.coronawarn.app?v=$TRACE_LOCATION_VERSION#"
    }
}
