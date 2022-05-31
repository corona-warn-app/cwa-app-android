package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.util.TimeStamper
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsExposureWindowRepository @Inject constructor(
    private val databaseFactory: AnalyticsExposureWindowDatabase.Factory,
    private val timeStamper: TimeStamper
) {
    private val database by lazy {
        databaseFactory.create()
    }

    private val dao by lazy {
        database.analyticsExposureWindowDao()
    }

    suspend fun getAllNew(): List<AnalyticsExposureWindowEntityWrapper> {
        return dao.getAllNew()
    }

    suspend fun addNew(analyticsExposureWindow: AnalyticsExposureWindow) {
        val hash = analyticsExposureWindow.sha256Hash()
        if (dao.getReported(hash) == null && dao.getNew(hash) == null) {
            val wrapper = analyticsExposureWindow.toWrapper(hash)
            dao.insert(listOf(wrapper))
        }
    }

    suspend fun moveToReported(
        wrapperEntities: List<AnalyticsExposureWindowEntityWrapper>
    ): List<AnalyticsReportedExposureWindowEntity> {
        return dao.moveToReported(wrapperEntities, timeStamper.nowUTC.toEpochMilli())
    }

    suspend fun rollback(
        wrappers: List<AnalyticsExposureWindowEntityWrapper>,
        reported: List<AnalyticsReportedExposureWindowEntity>
    ) {
        dao.rollback(wrappers, reported)
    }

    suspend fun deleteStaleData() {
        val timestamp = timeStamper.nowUTC.minus(Duration.ofDays(15)).toEpochMilli()
        dao.deleteReportedOlderThan(timestamp)
    }

    suspend fun deleteAllData() {
        val new = dao.getAllNew()
        dao.deleteExposureWindows(new.map { it.exposureWindowEntity })
        dao.deleteScanInstances(new.flatMap { it.scanInstanceEntities })
        dao.deleteReported(dao.getAllReported())
    }
}

@VisibleForTesting
internal fun AnalyticsExposureWindow.toWrapper(key: String) =
    AnalyticsExposureWindowEntityWrapper(
        exposureWindowEntity = AnalyticsExposureWindowEntity(
            sha256Hash = key,
            calibrationConfidence = calibrationConfidence,
            dateMillis = dateMillis,
            infectiousness = infectiousness,
            reportType = reportType,
            normalizedTime = normalizedTime,
            transmissionRiskLevel = transmissionRiskLevel
        ),
        scanInstanceEntities = analyticsScanInstances.map { it.toEntity(key) }
    )

private fun AnalyticsScanInstance.toEntity(foreignKey: String) =
    AnalyticsScanInstanceEntity(
        fkSha256Hash = foreignKey,
        minAttenuation = minAttenuation,
        typicalAttenuation = typicalAttenuation,
        secondsSinceLastScan = secondsSinceLastScan
    )
