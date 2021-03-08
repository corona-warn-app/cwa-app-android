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
    @ColumnInfo(name = "start") override val start: Instant,
    @ColumnInfo(name = "end") override val end: Instant
) : EventCheckIn,Parcelable
