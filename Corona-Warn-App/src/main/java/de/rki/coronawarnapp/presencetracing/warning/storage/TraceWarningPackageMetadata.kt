package de.rki.coronawarnapp.presencetracing.warning.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.HourInterval
import java.time.Instant

@Entity(tableName = "TraceWarningPackageMetadata")
data class TraceWarningPackageMetadata(
    @PrimaryKey @ColumnInfo(name = "id") val packageId: WarningPackageId,
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
    @ColumnInfo(name = "location") val location: LocationCode, // i.e. "DE"
    @ColumnInfo(name = "hourInterval") val hourInterval: HourInterval,
    @ColumnInfo(name = "eTag") val eTag: String? = null,
    @ColumnInfo(name = "downloaded") val isDownloaded: Boolean = false,
    @ColumnInfo(name = "emptyPkg") val isEmptyPkg: Boolean = false,
    @ColumnInfo(name = "processed") val isProcessed: Boolean = false
) {

    constructor(
        location: LocationCode,
        hourInterval: HourInterval,
        createdAt: Instant
    ) : this(
        packageId = calcluateId(location, hourInterval),
        location = location,
        hourInterval = hourInterval,
        createdAt = createdAt,
    )

    val fileName: String
        get() = "$packageId.bin"

    companion object {
        fun calcluateId(
            location: LocationCode,
            hourInterval: HourInterval
        ): WarningPackageId = "${location.identifier}_$hourInterval"
    }

    @Entity
    data class UpdateDownload(
        @PrimaryKey @ColumnInfo(name = "id") val packageId: WarningPackageId,
        @ColumnInfo(name = "eTag") val eTag: String?,
        @ColumnInfo(name = "downloaded") val isDownloaded: Boolean,
        @ColumnInfo(name = "emptyPkg") val isEmptyPkg: Boolean,
        @ColumnInfo(name = "processed") val isProcessed: Boolean,
    )

    @Entity
    data class UpdateProcessed(
        @PrimaryKey @ColumnInfo(name = "id") val packageId: WarningPackageId,
        @ColumnInfo(name = "processed") val isProcessed: Boolean,
    )
}
