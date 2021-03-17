package de.rki.coronawarnapp.eventregistration.checkins

import org.joda.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long,
    val guid: String,
    val version: Int,
    val type: Int,
    val description: String,
    val address: String,
    val traceLocationStart: Instant?,
    val traceLocationEnd: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val signature: String,
    val checkInStart: Instant,
    val checkInEnd: Instant?,
    val targetCheckInEnd: Instant?,
    val createJournalEntry: Boolean
)
