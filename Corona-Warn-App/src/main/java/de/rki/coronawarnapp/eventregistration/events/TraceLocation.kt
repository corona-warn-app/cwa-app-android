package de.rki.coronawarnapp.eventregistration.events

import org.joda.time.Instant

interface TraceLocation {
    val guid: String
    val version: Int
    val type: Type
    val description: String
    val address: String
    val startDate: Instant?
    val endDate: Instant?
    val defaultCheckInLengthInMinutes: Int?
    val signature: String

    enum class Type(val value: Int) {
        UNSPECIFIED(0),
        PERMANENT_OTHER(1),
        TEMPORARY_OTHER(2)
    }
}
