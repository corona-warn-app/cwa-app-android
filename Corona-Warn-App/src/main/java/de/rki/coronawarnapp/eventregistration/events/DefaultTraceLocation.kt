package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import org.joda.time.Instant

data class DefaultTraceLocation(
    override val guid: String,
    override val type: TraceLocationOuterClass.TraceLocationType,
    override val description: String,
    override val address: String,
    override val startDate: Instant?,
    override val endDate: Instant?,
    override val defaultCheckInLengthInMinutes: Int?,
    override val signature: String,
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
    signature = signature,
    version = version
)

// TODO: write tests for this mapper
fun TraceLocationOuterClass.SignedTraceLocation.toTraceLocation() = DefaultTraceLocation(
    guid = location.guid,
    type = location.type,
    description = location.description,
    address = location.address,
    startDate = Instant.ofEpochSecond(location.startTimestamp),
    endDate = Instant.ofEpochSecond(location.endTimestamp),
    defaultCheckInLengthInMinutes = location.defaultCheckInLengthInMinutes,
    signature = signature.toString(),
    version = location.version
)
