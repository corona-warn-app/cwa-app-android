package de.rki.coronawarnapp.eventregistration.checkins

import org.joda.time.Instant

@Suppress("LongParameterList")
class DefaultCheckIn(
    override val id: Long,
    override val guid: String,
    override val version: Int,
    override val type: Int,
    override val description: String,
    override val address: String,
    override val traceLocationStart: Instant?,
    override val traceLocationEnd: Instant?,
    override val defaultCheckInLengthInMinutes: Int?,
    override val signature: String,
    override val checkInStart: Instant,
    override val checkInEnd: Instant?,
    override val targetCheckInEnd: Instant?,
    override val createJournalEntry: Boolean
) : CheckIn
