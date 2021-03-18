package de.rki.coronawarnapp.eventregistration.checkins

import org.joda.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long,
    val guid: String,
    val guidHash: ByteArray,
    val version: Int,
    val type: Int,
    val description: String,
    val address: String,
    val traceLocationStart: Instant?,
    val traceLocationEnd: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val byteRepresentation: ByteArray,
    val signature: String,
    val checkInStart: Instant,
    val checkInEnd: Instant,
    val completed: Boolean,
    val createJournalEntry: Boolean
)
