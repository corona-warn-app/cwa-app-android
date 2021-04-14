package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import android.os.Parcel
import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<TraceLocationOuterClass.TraceLocation, TraceLocationParceler>()
@TypeParceler<TraceLocationOuterClass.QRCodePayload, QrCodePayloadParceler>()
data class VerifiedTraceLocation(
    private val protoQrCodePayload: TraceLocationOuterClass.QRCodePayload
) : Parcelable {
    @IgnoredOnParcel val traceLocation: TraceLocation = protoQrCodePayload.traceLocation()
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
