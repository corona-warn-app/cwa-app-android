package de.rki.coronawarnapp.eventregistration.checkins

import okio.ByteString
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
    val traceLocationBytes: ByteArray,
    val signature: ByteString,
    val checkInStart: Instant,
    val checkInEnd: Instant,
    val completed: Boolean,
    val createJournalEntry: Boolean
) {

    // we have to override it because of the array fields
    @SuppressWarnings("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckIn

        if (id != other.id) return false
        if (guid != other.guid) return false
        if (!guidHash.contentEquals(other.guidHash)) return false
        if (version != other.version) return false
        if (type != other.type) return false
        if (description != other.description) return false
        if (address != other.address) return false
        if (traceLocationStart != other.traceLocationStart) return false
        if (traceLocationEnd != other.traceLocationEnd) return false
        if (defaultCheckInLengthInMinutes != other.defaultCheckInLengthInMinutes) return false
        if (!traceLocationBytes.contentEquals(other.traceLocationBytes)) return false
        if (signature != other.signature) return false
        if (checkInStart != other.checkInStart) return false
        if (checkInEnd != other.checkInEnd) return false
        if (completed != other.completed) return false
        if (createJournalEntry != other.createJournalEntry) return false

        return true
    }
}
