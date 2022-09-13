package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import java.time.Instant

data class TraceLocationUserInput(
    val type: TraceLocationOuterClass.TraceLocationType,
    val description: String,
    val address: String,
    val startDate: Instant?,
    val endDate: Instant?,
    val defaultCheckInLengthInMinutes: Int
)
