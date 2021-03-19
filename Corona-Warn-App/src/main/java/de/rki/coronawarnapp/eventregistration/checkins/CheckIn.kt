package de.rki.coronawarnapp.eventregistration.checkins

import okio.ByteString
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import org.joda.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long,
    val guid: String,
    val guidHash: ByteString,
    val version: Int,
    val type: Int,
    val description: String,
    val address: String,
    val traceLocationStart: Instant?,
    val traceLocationEnd: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val traceLocationBytes: ByteString,
    val signature: ByteString,
    val checkInStart: Instant,
    val checkInEnd: Instant,
    val completed: Boolean,
    val createJournalEntry: Boolean
)

fun CheckIn.toEntity() = TraceLocationCheckInEntity(
    id = id,
    guid = guid,
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    signature = signature,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    targetCheckInEnd = targetCheckInEnd,
    createJournalEntry = createJournalEntry
)
