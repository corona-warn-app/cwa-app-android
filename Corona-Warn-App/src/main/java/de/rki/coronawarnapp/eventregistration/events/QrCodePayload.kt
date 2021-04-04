package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CWALocationData
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CrowdNotifierData
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64

fun TraceLocation.qrCodePayload(): QRCodePayload {
    val vendorData = CWALocationData.newBuilder()
        .setType(type)
        .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes ?: 0)
        .setVersion(TraceLocation.VERSION)
        .build()

    val crowdNotifierData = CrowdNotifierData.newBuilder()
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
