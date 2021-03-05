package de.rki.coronawarnapp.eventregistration.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
@Entity(tableName = "hostedEvents")
data class HostedEventEntity(

    @PrimaryKey @ColumnInfo(name = "guid") override val guid: String,
    @ColumnInfo(name = "description") override val description: String,
    @ColumnInfo(name = "location") override val location: String,
    @ColumnInfo(name = "startTime") override val startTime: Instant?,
    @ColumnInfo(name = "endTime") override val endTime: Instant?,
    @ColumnInfo(name = "defaultCheckInLength") override val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "signature") override val signature: String

) : HostedEvent, Parcelable

fun HostedEvent.toHostedEventEntity(): HostedEventEntity =
    HostedEventEntity(
        guid = guid,
        description = description,
        location = "hardcodedLocation", // event.location will be in the protobuf at some point in the future
        startTime = startTime,
        endTime = endTime,
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
        signature = signature
    )
