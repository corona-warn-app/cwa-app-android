package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import okio.ByteString
import okio.ByteString.Companion.encode
import org.joda.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long = 0L,
    val traceLocationId: ByteString = "TODO: calculate".encode(),
    val traceLocationIdHash: ByteString = "TODO: calculate".encode(),
    val version: Int,
    val type: Int,
    val description: String,
    val address: String,
    val traceLocationStart: Instant?,
    val traceLocationEnd: Instant?,
    val defaultCheckInLengthInMinutes: Int?,
    val cryptographicSeed: ByteString,
    val cnPublicKey: String,
    val checkInStart: Instant,
    val checkInEnd: Instant,
    val completed: Boolean,
    val createJournalEntry: Boolean
) {
    // TODO calculate hash for matching
    // val locationGuidHash: com.google.protobuf.ByteString by lazy { copyFromUtf8(guid.toSHA256()) }
}

fun CheckIn.toEntity() = TraceLocationCheckInEntity(
    id = id,
    traceLocationIdBase64 = traceLocationId.base64(),
    traceLocationIdHashBase64 = traceLocationIdHash.base64(),
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    cryptographicSeedBase64 = cryptographicSeed.base64(),
    cnPublicKey = cnPublicKey,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry
)
