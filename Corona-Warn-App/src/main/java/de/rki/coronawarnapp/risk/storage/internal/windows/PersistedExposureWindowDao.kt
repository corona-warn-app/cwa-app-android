package de.rki.coronawarnapp.risk.storage.internal.windows

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exposurewindows")
data class PersistedExposureWindowDao(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "riskLevelResultId") val riskLevelResultId: String,
    @ColumnInfo(name = "dateMillisSinceEpoch") val dateMillisSinceEpoch: Long,
    @ColumnInfo(name = "calibrationConfidence") val calibrationConfidence: Int,
    @ColumnInfo(name = "infectiousness") val infectiousness: Int,
    @ColumnInfo(name = "reportType") val reportType: Int
) {

    @Entity(
        tableName = "scaninstances",
        foreignKeys = [
            ForeignKey(
                onDelete = CASCADE,
                entity = PersistedExposureWindowDao::class,
                parentColumns = ["id"],
                childColumns = ["exposureWindowId"]
            )
        ],
        indices = [Index("exposureWindowId")]
    )
    data class PersistedScanInstance(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
        @ColumnInfo(name = "exposureWindowId") val exposureWindowId: Long,
        @ColumnInfo(name = "minAttenuationDb") val minAttenuationDb: Int,
        @ColumnInfo(name = "secondsSinceLastScan") val secondsSinceLastScan: Int,
        @ColumnInfo(name = "typicalAttenuationDb") val typicalAttenuationDb: Int
    )
}
