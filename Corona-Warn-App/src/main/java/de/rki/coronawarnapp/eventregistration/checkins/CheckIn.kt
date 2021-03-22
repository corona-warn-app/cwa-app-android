package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString.copyFromUtf8
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import okio.ByteString
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
    val traceLocationBytes: ByteString,
    val signature: ByteString,
    val checkInStart: Instant,
    val checkInEnd: Instant,
    val completed: Boolean,
    val createJournalEntry: Boolean
) {
    val locationGuidHash: com.google.protobuf.ByteString by lazy { copyFromUtf8(guid.toSHA256()) }
}
