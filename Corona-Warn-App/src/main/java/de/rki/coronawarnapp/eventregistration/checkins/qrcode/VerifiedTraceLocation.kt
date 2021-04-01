package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcel
import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

@Parcelize
@TypeParceler<TraceLocationOuterClass.TraceLocation, TraceLocationParceler>()
@TypeParceler<TraceLocationOuterClass.QRCodePayload, QrCodePayloadParceler>()
data class VerifiedTraceLocation(
    private val protoQrCodePayload: TraceLocationOuterClass.QRCodePayload
) : Parcelable {

    @IgnoredOnParcel private val vendorData by lazy {
        TraceLocationOuterClass.CWALocationData.parseFrom(protoQrCodePayload.vendorData)
    }

    @IgnoredOnParcel val traceLocation: TraceLocation by lazy {

        TraceLocation(
            version = protoQrCodePayload.version,
            type = vendorData.type,
            description = protoQrCodePayload.locationData.description,
            address = protoQrCodePayload.locationData.address,
            startDate = protoQrCodePayload.locationData.startTimestamp.toInstant(),
            endDate = protoQrCodePayload.locationData.endTimestamp.toInstant(),
            defaultCheckInLengthInMinutes = vendorData.defaultCheckInLengthInMinutes,
            cryptographicSeed = protoQrCodePayload.crowdNotifierData.cryptographicSeed.toByteArray().toByteString(),
            cnPublicKey = protoQrCodePayload.crowdNotifierData.publicKey.toStringUtf8()
        )
    }

    /**
     * Converts time in seconds into [Instant]
     */
    private fun Long.toInstant() =
        if (this == 0L) null else Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(this))
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
