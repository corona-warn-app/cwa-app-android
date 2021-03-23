package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import okio.ByteString
import org.joda.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long = 0L,
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
    guidHashBase64 = guidHash.base64(),
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    traceLocationBytesBase64 = traceLocationBytes.base64(),
    signatureBase64 = signature.base64(),
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry
)
