package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
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
    @ColumnInfo(name = "signature") val signature: String,
    @ColumnInfo(name = "checkInStart") val checkInStart: Instant,
    @ColumnInfo(name = "checkInEnd") val checkInEnd: Instant?,
    @ColumnInfo(name = "targetCheckInEnd") val targetCheckInEnd: Instant?,
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
    signature = signature,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    targetCheckInEnd = targetCheckInEnd,
    createJournalEntry = createJournalEntry
)
