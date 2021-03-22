package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant

@Entity(tableName = "checkin")
data class TraceLocationCheckInEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "guid") val guid: String,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "traceLocationStart") val traceLocationStart: Instant?,
    @ColumnInfo(name = "traceLocationEnd") val traceLocationEnd: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "traceLocationBytes") val traceLocationBytesBase64: String,
    @ColumnInfo(name = "signature") val signatureBase64: String,
    @ColumnInfo(name = "checkInStart") val checkInStart: Instant,
    @ColumnInfo(name = "checkInEnd") val checkInEnd: Instant,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "createJournalEntry") val createJournalEntry: Boolean
)

fun TraceLocationCheckInEntity.toCheckIn() = CheckIn(
    id = id,
    guid = guid,
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    traceLocationBytes = traceLocationBytesBase64.decodeBase64()!!,
    signature = signatureBase64.decodeBase64()!!,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry
)
