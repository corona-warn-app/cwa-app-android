package de.rki.coronawarnapp.eventregistration.checkins

import org.joda.time.Instant

interface EventCheckIn {
    val id: Long
    val guid: String
    val startTime: Instant
    val endTime: Instant
}
