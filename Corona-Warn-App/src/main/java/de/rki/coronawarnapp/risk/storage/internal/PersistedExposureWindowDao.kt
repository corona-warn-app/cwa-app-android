package de.rki.coronawarnapp.risk.storage.internal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.storage.internal.PersistedExposureWindowDao.PersistedScanInstance
import java.util.UUID

@Entity(tableName = "exposurewindows")
data class PersistedExposureWindowDao(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "riskLevelResultId") val riskLevelResultId: String,
    @ColumnInfo(name = "dateMillisSinceEpoch") val dateMillisSinceEpoch: Long,
    @ColumnInfo(name = "calibrationConfidence") val calibrationConfidence: Int,
    @ColumnInfo(name = "infectiousness") val infectiousness: Int,
    @ColumnInfo(name = "reportType") val reportType: Int,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "exposureWindowId"
//    ) val scanInstances: List<PersistedScanInstance> // FIXME
) {

    fun toExposureWindow(): ExposureWindow = ExposureWindow.Builder().apply {
        setDateMillisSinceEpoch(dateMillisSinceEpoch)
        setCalibrationConfidence(calibrationConfidence)
        setInfectiousness(infectiousness)
        setReportType(reportType)
//        setScanInstances(scanInstances.map { it.toScanInstance() }) // FIXME
    }.build()

    @Entity(tableName = "scaninstances")
    data class PersistedScanInstance(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
        @ColumnInfo(name = "exposureWindowId") val exposureWindowId: Long,
        @ColumnInfo(name = "minAttenuationDb") val minAttenuationDb: Int,
        @ColumnInfo(name = "secondsSinceLastScan") val secondsSinceLastScan: Int,
        @ColumnInfo(name = "typicalAttenuationDb") val typicalAttenuationDb: Int
    ) {
        fun toScanInstance(): ScanInstance = ScanInstance.Builder().apply {
            setMinAttenuationDb(minAttenuationDb)
            setSecondsSinceLastScan(secondsSinceLastScan)

            setTypicalAttenuationDb(typicalAttenuationDb)
        }.build()
    }
}

fun ExposureWindow.toPersistedExposureWindow(
    id: String = UUID.randomUUID().toString(),
    riskLevelResultId: String
) = PersistedExposureWindowDao(
    riskLevelResultId = riskLevelResultId,
    dateMillisSinceEpoch = this.dateMillisSinceEpoch,
    calibrationConfidence = this.calibrationConfidence,
    infectiousness = this.infectiousness,
    reportType = this.reportType,
//    scanInstances = this.scanInstances.map { it.toPersistedScanInstance(exposureWindowId = id) } // FIXME
)

fun ScanInstance.toPersistedScanInstance(
    exposureWindowId: Long
) = PersistedScanInstance(
    exposureWindowId = exposureWindowId,
    minAttenuationDb = this.minAttenuationDb,
    secondsSinceLastScan = this.secondsSinceLastScan,
    typicalAttenuationDb = this.typicalAttenuationDb
)

