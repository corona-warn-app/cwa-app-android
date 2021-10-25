package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.TimeStamper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTestResultEWRepository @Inject constructor(
    private val databaseFactory: AnalyticsTestResultEwDatabase.Factory,
    private val timeStamper: TimeStamper
) {
    private val database by lazy {
        databaseFactory.create()
    }

    private val dao by lazy {
        database.analyticsExposureWindowDao()
    }

    suspend fun getAll(): List<AnalyticsTestResultEwEntityWrapper> {
        return dao.getAll()
    }

    suspend fun add(type: CoronaTest.Type, exposureWindows: List<ExposureWindow>) {
        // TODO covert to dao.insert(analyticsExposureWindows)
    }

    suspend fun deleteAll(type: CoronaTest.Type) {
        val wrapperList = dao.getAll()
        dao.deleteExposureWindows(wrapperList.map { it.exposureWindowEntity })
        //dao.deleteScanInstances(wrapperList.flatMap { it.scanInstanceEntities })
    }
}

private fun AnalyticsScanInstance.toEntity(foreignKey: String) =
    AnalyticsScanInstanceEntity(
        fkSha256Hash = foreignKey,
        minAttenuation = minAttenuation,
        typicalAttenuation = typicalAttenuation,
        secondsSinceLastScan = secondsSinceLastScan
    )
