package de.rki.coronawarnapp.presencetracing.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import java.time.Instant

@Entity(tableName = "traceLocations")
data class TraceLocationEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "type") val type: TraceLocationOuterClass.TraceLocationType,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "startDate") val startDate: Instant?,
    @ColumnInfo(name = "endDate") val endDate: Instant?,
    @ColumnInfo(name = "defaultCheckInLengthInMinutes") val defaultCheckInLengthInMinutes: Int?,
    @ColumnInfo(name = "cryptographicSeedBase64") val cryptographicSeedBase64: String,
    @ColumnInfo(name = "cnPublicKey") val cnPublicKey: String
)

fun TraceLocation.toTraceLocationEntity(): TraceLocationEntity =
    TraceLocationEntity(
        id = id,
        type = type,
        description = description,
        address = address,
        startDate = startDate,
        endDate = endDate,
        defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
        cryptographicSeedBase64 = cryptographicSeed.base64(),
        cnPublicKey = cnPublicKey,
        version = version
    )
