package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocationIdHash
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import okio.ByteString
import java.time.Instant

@Suppress("LongParameterList")
data class CheckIn(
    val id: Long = 0L,
    val traceLocationId: TraceLocationId,
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
    val createJournalEntry: Boolean,
    val isSubmitted: Boolean = false,
    val hasSubmissionConsent: Boolean = false,
) {
    /**
     *  Returns SHA-256 hash of [traceLocationId] which itself may also be SHA-256 hash.
     *  For privacy reasons TraceTimeIntervalWarnings only offer a hash of the actual locationId.
     *
     *  @see [de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation]
     */
    val traceLocationIdHash by lazy { traceLocationId.toTraceLocationIdHash() }
}

fun CheckIn.toEntity() = TraceLocationCheckInEntity(
    id = id,
    traceLocationIdBase64 = traceLocationId.base64(),
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
    createJournalEntry = createJournalEntry,
    isSubmitted = isSubmitted,
    hasSubmissionConsent = hasSubmissionConsent,
)
