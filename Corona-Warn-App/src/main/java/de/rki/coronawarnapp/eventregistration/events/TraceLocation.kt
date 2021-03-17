package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import org.joda.time.Instant

interface TraceLocation {
    val guid: String
    val version: Int
    val type: TraceLocationOuterClass.TraceLocationType
    val description: String
    val address: String
    val startDate: Instant?
    val endDate: Instant?
    val defaultCheckInLengthInMinutes: Int?
    val signature: String
}
