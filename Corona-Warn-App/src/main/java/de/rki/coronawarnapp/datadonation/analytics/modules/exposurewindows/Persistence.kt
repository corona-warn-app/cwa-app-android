package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

@Dao
abstract class ExposureWindowContributionDao {
    @Query("SELECT * FROM exposureWindows")
    abstract suspend fun allNewEntries(): List<ExposureWindowWrapper>

    @Query("DELETE FROM exposureWindows")
    abstract suspend fun delete(entities: List<ExposureWindowWrapper>)

    @Insert
    abstract suspend fun insert(entity: ExposureWindowWrapper): Long

    @Insert
    abstract suspend fun insertReported(entities: List<ReportedExposureWindowEntity>): Long

    @Transaction
    suspend fun moveToReported(entities: List<ExposureWindowWrapper>, timestamp: Long) {
        val reported = entities.map {
            ReportedExposureWindowEntity(
                it.exposureWindow.sha256Hash,
                timestamp
            )
        }
        insertReported(reported)
        delete(entities)
    }
}

class ExposureWindowWrapper(
    @Embedded val scanInstances: List<ScanInstanceEntity>,
    @Relation(parentColumn = "sha256Hash", entityColumn = "fkSha256Hash")
    val exposureWindow: ExposureWindowEntity
) {
    val asExposureWindowContribution = ExposureWindowContribution(
        calibrationConfidence = exposureWindow.calibrationConfidence,
        dateMillis = exposureWindow.dateMillis,
        infectiousness = exposureWindow.infectiousness,
        reportType = exposureWindow.reportType,
        scanInstances = scanInstances.map { it.asScanInstanceContribution() },
        normalizedTime = exposureWindow.normalizedTime,
        transmissionRiskLevel = exposureWindow.transmissionRiskLevel
    )
}

@Entity(tableName = "exposureWindows")
data class ExposureWindowEntity(
    @PrimaryKey(autoGenerate = false) val sha256Hash: String,
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

@Entity(tableName = "scanInstances")
data class ScanInstanceEntity(
    @ForeignKey(
        entity = ExposureWindowEntity::class,
        parentColumns = ["sha256Hash"],
        childColumns = ["fkSha256Hash"],
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )
    val fkSha256Hash: String,
    val minAttenuation: Int,
    val typicalAttenuation: Int,
    val secondsSinceLastScan: Int
)

@Entity(tableName = "reportedExposureWindows")
data class ReportedExposureWindowEntity(
    @PrimaryKey(autoGenerate = false) val sha256Hash: String,
    val timestamp: Long
)

private fun ScanInstanceEntity.asScanInstanceContribution() = ScanInstanceContribution(
    minAttenuation = minAttenuation,
    typicalAttenuation = typicalAttenuation,
    secondsSinceLastScan = secondsSinceLastScan
)
