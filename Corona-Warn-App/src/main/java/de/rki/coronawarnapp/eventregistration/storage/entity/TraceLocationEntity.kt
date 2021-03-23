package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import org.joda.time.Instant

@Entity(tableName = "traceLocations")
data class TraceLocationEntity(

    @PrimaryKey @ColumnInfo(name = "guid") val guid: String,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "type") val type: TraceLocationOuterClass.TraceLocationType,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "startDate") val startDate: Instant?,
    @ColumnInfo(name = "endDate") val endDate: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "byteRepresentation") val byteRepresentationBase64: String,
    @ColumnInfo(name = "signature") val signatureBase64: String
)

fun TraceLocation.toTraceLocationEntity(): TraceLocationEntity =
    TraceLocationEntity(
        guid = guid,
        type = type,
        description = description,
        address = address,
        startDate = startDate,
        endDate = endDate,
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
        byteRepresentationBase64 = byteRepresentation.base64(),
        signatureBase64 = signature.base64(),
        version = version
    )
