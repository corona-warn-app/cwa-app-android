package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcel
import android.os.Parcelable
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okio.ByteString
import okio.ByteString.Companion.encode
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import java.util.concurrent.TimeUnit

@Parcelize
@TypeParceler<TraceLocationOuterClass.SignedTraceLocation, SignedTraceLocationParceler>()
@TypeParceler<TraceLocationOuterClass.TraceLocation, TraceLocationParceler>()
data class VerifiedTraceLocation(
    private val protoSignedTraceLocation: TraceLocationOuterClass.SignedTraceLocation,
    private val protoTraceLocation: TraceLocationOuterClass.TraceLocation
) : Parcelable {

    val traceLocationBytes: ByteString
        get() = protoSignedTraceLocation.location.toByteArray().toByteString()

    val signature: ByteString
        get() = protoSignedTraceLocation.signature.toByteArray().toByteString()

    @IgnoredOnParcel val traceLocation: TraceLocation by lazy {
        TraceLocation(
            // guid = protoTraceLocation.guid,
            version = protoTraceLocation.version,
            type = protoTraceLocation.type,
            description = protoTraceLocation.description,
            address = protoTraceLocation.address,
            startDate = protoTraceLocation.startTimestamp.toInstant(),
            endDate = protoTraceLocation.endTimestamp.toInstant(),
            defaultCheckInLengthInMinutes = protoTraceLocation.defaultCheckInLengthInMinutes,
            // byteRepresentation = traceLocationBytes,
            // signature = signature,
            cryptographicSeed = "".encode(),
            cnPublicKey = ""
        )
    }

    /**
     * Converts time in seconds into [Instant]
     */
    private fun Long.toInstant() =
        if (this == 0L) null else Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(this))
}

private object SignedTraceLocationParceler : Parceler<TraceLocationOuterClass.SignedTraceLocation> {
    override fun create(parcel: Parcel): TraceLocationOuterClass.SignedTraceLocation {
        val rawSignedTraceLocation = ByteArray(parcel.readInt())
        parcel.readByteArray(rawSignedTraceLocation)
        return TraceLocationOuterClass.SignedTraceLocation.parseFrom(rawSignedTraceLocation)
    }

    override fun TraceLocationOuterClass.SignedTraceLocation.write(parcel: Parcel, flags: Int) {
        val rawSignedTraceLocation = toByteArray()
        parcel.writeInt(rawSignedTraceLocation.size)
        parcel.writeByteArray(rawSignedTraceLocation)
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
