package de.rki.coronawarnapp.eventregistration.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
@Entity(tableName = "traceLocations")
data class TraceLocationEntity(

    @PrimaryKey @ColumnInfo(name = "guid") override val guid: String,
    @ColumnInfo(name = "version") override val version: Int,
    @ColumnInfo(name = "type") override val type: TraceLocation.Type,
    @ColumnInfo(name = "description") override val description: String,
    @ColumnInfo(name = "location") override val address: String,
    @ColumnInfo(name = "startTime") override val startDate: Instant?,
    @ColumnInfo(name = "endTime") override val endDate: Instant?,
    @ColumnInfo(name = "defaultCheckInLength") override val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "signature") override val signature: String

) : TraceLocation, Parcelable

fun TraceLocation.toTraceLocationEntity(): TraceLocationEntity =
    TraceLocationEntity(
        guid = guid,
        type = type,
        description = description,
        address = address,
        startDate = startDate,
        endDate = endDate,
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
        signature = signature,
        version = version
    )
