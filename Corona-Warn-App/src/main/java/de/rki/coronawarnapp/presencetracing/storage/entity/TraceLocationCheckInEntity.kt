package de.rki.coronawarnapp.presencetracing.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import okio.ByteString.Companion.decodeBase64
import java.time.Instant

@Entity(tableName = "checkin")
data class TraceLocationCheckInEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "traceLocationIdBase64") val traceLocationIdBase64: String,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "traceLocationStart") val traceLocationStart: Instant?,
    @ColumnInfo(name = "traceLocationEnd") val traceLocationEnd: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "cryptographicSeedBase64") val cryptographicSeedBase64: String,
    @ColumnInfo(name = "cnPublicKey") val cnPublicKey: String,
    @ColumnInfo(name = "checkInStart") val checkInStart: Instant,
    @ColumnInfo(name = "checkInEnd") val checkInEnd: Instant,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "createJournalEntry") val createJournalEntry: Boolean,
    @ColumnInfo(name = "submitted") val isSubmitted: Boolean,
    @ColumnInfo(name = "submissionConsent") val hasSubmissionConsent: Boolean,
) {

    @Entity
    data class SubmissionUpdate(
        @PrimaryKey @ColumnInfo(name = "id") val checkInId: Long,
        @ColumnInfo(name = "submitted") val isSubmitted: Boolean,
        @ColumnInfo(name = "submissionConsent") val hasSubmissionConsent: Boolean,
    )

    @Entity
    data class SubmissionConsentUpdate(
        @PrimaryKey @ColumnInfo(name = "id") val checkInId: Long,
        @ColumnInfo(name = "submissionConsent") val hasSubmissionConsent: Boolean,
    )
}

fun TraceLocationCheckInEntity.toCheckIn() = CheckIn(
    id = id,
    traceLocationId = traceLocationIdBase64.decodeBase64()!!,
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    cryptographicSeed = cryptographicSeedBase64.decodeBase64()!!,
    cnPublicKey = cnPublicKey,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry,
    isSubmitted = isSubmitted,
    hasSubmissionConsent = hasSubmissionConsent
)
