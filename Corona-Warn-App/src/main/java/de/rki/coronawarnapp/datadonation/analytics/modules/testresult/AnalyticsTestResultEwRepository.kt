package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsScanInstance
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.AnalyticsScanInstanceEntity
import de.rki.coronawarnapp.util.TimeStamper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTestResultEwRepository @Inject constructor(
    private val databaseFactory: AnalyticsTestResultEwDatabase.Factory,
    private val timeStamper: TimeStamper
) {
    private val database by lazy {
        databaseFactory.create()
    }

    private val dao by lazy {
        database.analyticsTestResultEWDao()
    }

    suspend fun getAll(type: CoronaTest.Type): List<AnalyticsTestResultEwEntityWrapper> {
        return dao.getAll(type)
    }

    suspend fun add(type: CoronaTest.Type, exposureWindows: List<ExposureWindow>) {
        val wrappers = exposureWindows.map {
            it.toWrapper(type)
        }
        dao.insert(wrappers)
    }

    suspend fun deleteAll(type: CoronaTest.Type) {
        val wrapperList = dao.getAll(type)
        dao.deleteAll(wrapperList.map { it.exposureWindowEntity })
    }
}

private fun AnalyticsScanInstance.toEntity(foreignKey: String) =
    AnalyticsScanInstanceEntity(
        fkSha256Hash = foreignKey,
        minAttenuation = minAttenuation,
        typicalAttenuation = typicalAttenuation,
        secondsSinceLastScan = secondsSinceLastScan
    )

private fun ExposureWindow.toWrapper(type: CoronaTest.Type) = AnalyticsTestResultEwEntityWrapper(
    exposureWindowEntity = toEntity(type),
    scanInstanceEntities = scanInstances.map { it.toEntity() }
)

private fun ExposureWindow.toEntity(type: CoronaTest.Type) = AnalyticsTestResultEwEntity(
    id = null,
    testType = type,
    calibrationConfidence = calibrationConfidence,
    dateMillis = dateMillisSinceEpoch,
    infectiousness = infectiousness,
    reportType = reportType,
    // TODO ?
    normalizedTime = 0.0,
    transmissionRiskLevel = 0
)

private fun ScanInstance.toEntity() = AnalyticsTestResultScanInstanceEntity(
    fkId = null,
    minAttenuation = minAttenuationDb,
    typicalAttenuation = typicalAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)
