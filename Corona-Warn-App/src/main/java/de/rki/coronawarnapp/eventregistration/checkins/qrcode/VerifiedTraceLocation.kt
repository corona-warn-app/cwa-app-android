package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcel
import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.toOkioByteString
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

import okio.ByteString.Companion.toByteString

@Parcelize
@TypeParceler<TraceLocationOuterClass.TraceLocation, TraceLocationParceler>()
@TypeParceler<TraceLocationOuterClass.QRCodePayload, QrCodePayloadParceler>()
data class VerifiedTraceLocation(
    private val protoQrCodePayload: TraceLocationOuterClass.QRCodePayload
) : Parcelable {
    @IgnoredOnParcel val traceLocation: TraceLocation = protoQrCodePayload.traceLocation()

    private fun TraceLocationOuterClass.QRCodePayload.traceLocation(): TraceLocation {
        val cwaLocationData = TraceLocationOuterClass.CWALocationData.parseFrom(protoQrCodePayload.vendorData)
        return TraceLocation(
            version = version,
            type = cwaLocationData.type,
            defaultCheckInLengthInMinutes = cwaLocationData.defaultCheckInLengthInMinutes,
            description = locationData.description,
            address = locationData.address,
            startDate = locationData.startTimestamp.secondsToInstant(),
            endDate = locationData.endTimestamp.secondsToInstant(),
            cryptographicSeed = crowdNotifierData.cryptographicSeed.toByteArray().toByteString(),
            cnPublicKey = crowdNotifierData.publicKey.toOkioByteString().base64()
        )
    }
}

private object TraceLocationParceler : Parceler<TraceLocationOuterClass.TraceLocation> {
    override fun create(parcel: Parcel): TraceLocationOuterClass.TraceLocation {
        val rawTraceLocation = ByteArray(parcel.readInt())
        parcel.readByteArray(rawTraceLocation)
        return TraceLocationOuterClass.TraceLocation.parseFrom(rawTraceLocation)
    }

    override fun TraceLocationOuterClass.TraceLocation.write(parcel: Parcel, flags: Int) {
        val rawTraceLocation = toByteArray()
        parcel.writeInt(rawTraceLocation.size)
        parcel.writeByteArray(rawTraceLocation)
    }
}

private object QrCodePayloadParceler : Parceler<TraceLocationOuterClass.QRCodePayload> {
    override fun create(parcel: Parcel): TraceLocationOuterClass.QRCodePayload {
        val rawSignedTraceLocation = ByteArray(parcel.readInt())
        parcel.readByteArray(rawSignedTraceLocation)
        return TraceLocationOuterClass.QRCodePayload.parseFrom(rawSignedTraceLocation)
    }

    override fun TraceLocationOuterClass.QRCodePayload.write(parcel: Parcel, flags: Int) {
        val rawSignedTraceLocation = toByteArray()
        parcel.writeInt(rawSignedTraceLocation.size)
        parcel.writeByteArray(rawSignedTraceLocation)
    }
}
