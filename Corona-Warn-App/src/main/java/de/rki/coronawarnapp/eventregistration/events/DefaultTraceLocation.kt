package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import kotlinx.parcelize.Parcelize
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant

const val TRACE_LOCATION_VERSION = 1

@Parcelize
data class DefaultTraceLocation(
    override val guid: String,
    override val type: TraceLocationOuterClass.TraceLocationType,
    override val description: String,
    override val address: String,
    override val startDate: Instant?,
    override val endDate: Instant?,
    override val defaultCheckInLengthInMinutes: Int?,
    override val signature: ByteString,
    override val version: Int = TRACE_LOCATION_VERSION,
) : TraceLocation

fun List<TraceLocationEntity>.toTraceLocations() = this.map { it.toTraceLocation() }

fun TraceLocationEntity.toTraceLocation() = DefaultTraceLocation(
    guid = guid,
    type = type,
    description = description,
    address = address,
    startDate = startDate,
    endDate = endDate,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    signature = signatureBase64.decodeBase64()!!,
    version = version
)

/*fun SignedEventOuterClass.SignedEvent.toHostedEvent(): TraceLocation =
    DefaultTraceLocation(
        guid = event.guid.toString(),
        type = enumValues<TraceLocation.Type>()[type],
        description = event.description,
        address = "hardcodedLocation", // event.location,
        // backend needs UNIX timestamp in seconds, so we have to multiply it by 1000 to get milliseconds
        startDate = Instant.ofEpochMilli(event.start.toLong() * 1000),
        endDate = Instant.ofEpochMilli(event.end.toLong() * 1000),
        defaultCheckInLengthInMinutes = event.defaultCheckInLengthInMinutes,
        signature = signature.toString()
    )*/
