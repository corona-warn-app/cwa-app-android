package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.Instant

@Entity(tableName = "checkin")
data class TraceLocationCheckInEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "guid") val guid: String,
    @ColumnInfo(name = "guidHash") val guidHash: ByteArray,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "traceLocationStart") val traceLocationStart: Instant?,
    @ColumnInfo(name = "traceLocationEnd") val traceLocationEnd: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "traceLocationBytes") val traceLocationBytes: ByteArray,
    @ColumnInfo(name = "signature") val signatureBase64: String,
    @ColumnInfo(name = "checkInStart") val checkInStart: Instant,
    @ColumnInfo(name = "checkInEnd") val checkInEnd: Instant,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "createJournalEntry") val createJournalEntry: Boolean
) {

    // we have to override it because of the array fields
    @SuppressWarnings("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TraceLocationCheckInEntity

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
        if (signatureBase64 != other.signatureBase64) return false
        if (checkInStart != other.checkInStart) return false
        if (checkInEnd != other.checkInEnd) return false
        if (completed != other.completed) return false
        if (createJournalEntry != other.createJournalEntry) return false

        return true
    }
}
