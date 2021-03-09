package de.rki.coronawarnapp.eventregistration.checkins

import org.joda.time.Instant

interface TraceLocationCheckIn {
    val id: Long
    val guid: String
    val startTime: Instant
    val endTime: Instant
}
