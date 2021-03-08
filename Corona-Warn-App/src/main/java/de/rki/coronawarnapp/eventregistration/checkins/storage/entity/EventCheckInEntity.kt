package de.rki.coronawarnapp.eventregistration.checkins.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.checkins.EventCheckIn
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
@Entity(tableName = "checkin")
data class EventCheckInEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val id: Long = 0L,
    @ColumnInfo(name = "guid") override val guid: String,
    @ColumnInfo(name = "version") override val version: Int,
    @ColumnInfo(name = "type") override val type: Int,
    @ColumnInfo(name = "description") override val description: String,
    @ColumnInfo(name = "address") override val address: String,
    @ColumnInfo(name = "traceLocationStart") override val traceLocationStart: Instant?,
    @ColumnInfo(name = "traceLocationEnd") override val traceLocationEnd: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") override val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "signature") override val signature: String,
    @ColumnInfo(name = "checkInStart") override val checkInStart: Instant,
    @ColumnInfo(name = "checkInEnd") override val checkInEnd: Instant?,
    @ColumnInfo(name = "targetCheckInEnd") override val targetCheckInEnd: Instant?,
    @ColumnInfo(name = "createJournalEntry") override val createJournalEntry: Boolean
) : EventCheckIn, Parcelable
