package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import okio.ByteString.Companion.toByteString
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
) {
    // todo how to determine hash string
    val traceLocationGuidHash = guid.toSHA256().toByteArray().toByteString()
}
