package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import org.joda.time.Instant

const val TRACE_LOCATION_VERSION = 1

data class DefaultTraceLocation(
    override val guid: String,
    override val type: TraceLocation.Type,
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
