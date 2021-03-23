package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocation.parseFrom
import kotlinx.parcelize.Parcelize
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant

const val TRACE_LOCATION_VERSION = 1

@Parcelize
data class TraceLocation(
    val guid: String,
    val type: TraceLocationOuterClass.TraceLocationType,
    val description: String,
    val address: String,
    val startDate: Instant?,
    val endDate: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val byteRepresentation: ByteString,
    val signature: ByteString,
    val version: Int = TRACE_LOCATION_VERSION,
) : Parcelable {

    fun isBeforeStartTime(now: Instant): Boolean = startDate?.isAfter(now) ?: false

    fun isAfterEndTime(now: Instant): Boolean = endDate?.isBefore(now) ?: false
}

fun List<TraceLocationEntity>.toTraceLocations() = this.map { it.toTraceLocation() }

fun TraceLocationEntity.toTraceLocation() = TraceLocation(
    guid = guid,
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    byteRepresentation = byteRepresentationBase64.decodeBase64()!!,
    signature = signatureBase64.decodeBase64()!!,
    version = version
)

fun TraceLocationOuterClass.SignedTraceLocation.toTraceLocation(): TraceLocation {

    val traceLocation = parseFrom(location)

    fun Long.getInstantOrNull() = if (this == 0L) null else Instant.ofEpochSecond(this)

    return TraceLocation(
        guid = traceLocation.guid,
        type = traceLocation.type,
        description = traceLocation.description,
        address = traceLocation.address,
        startDate = traceLocation.startTimestamp.getInstantOrNull(),
        endDate = traceLocation.endTimestamp.getInstantOrNull(),
        defaultCheckInLengthInMinutes = traceLocation.defaultCheckInLengthInMinutes,
        signature = signature.toByteArray().toByteString(),
        version = traceLocation.version
    )
}
