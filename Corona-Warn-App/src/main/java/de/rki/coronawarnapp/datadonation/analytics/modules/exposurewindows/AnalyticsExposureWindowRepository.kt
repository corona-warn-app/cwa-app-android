package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.util.TimeStamper
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsExposureWindowRepository @Inject constructor(
    private val databaseFactory: AnalyticsExposureWindowDatabase.Factory,
    val timeStamper: TimeStamper
) {
    private val database by lazy {
        databaseFactory.create()
    }

    private val dao by lazy {
        database.exposureWindowContributionDao()
    }

    suspend fun getAllNew(): List<AnalyticsExposureWindowEntityWrapper> {
        return dao.getAllNew()
    }

    suspend fun addNew(analyticsExposureWindow: AnalyticsExposureWindow) {
        analyticsExposureWindow.sha256Hash()?.let { hash ->
            if (dao.getReported(hash) == null) {
                val wrapper = analyticsExposureWindow.toWrapper(hash)
                dao.insert(listOf(wrapper))
            }
        }
    }

    suspend fun moveToReported(wrapperEntities: List<AnalyticsExposureWindowEntityWrapper>):
        List<AnalyticsReportedExposureWindowEntity> {
        return dao.moveToReported(wrapperEntities, timeStamper.nowUTC.millis)
    }

    suspend fun rollback(
        wrappers: List<AnalyticsExposureWindowEntityWrapper>,
        reported: List<AnalyticsReportedExposureWindowEntity>
    ) {
        dao.rollback(wrappers, reported)
    }

    suspend fun deleteAllData() {
        val new = dao.getAllNew()
        dao.deleteExposureWindows(new.map { it.exposureWindowEntity })
        dao.deleteScanInstances(new.flatMap { it.scanInstanceEntities })
        dao.deleteReported(dao.getAllReported())
    }
}

private fun AnalyticsExposureWindow.sha256Hash() = toString().getSha256Hash()

private fun String.getSha256Hash(): String? {
    return try {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.reset()
        messageDigest.digest(toByteArray()).toHexString()
    } catch (e1: NoSuchAlgorithmException) {
        e1.printStackTrace()
        null
    }
}

private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

private fun AnalyticsExposureWindow.toWrapper(key: String) =
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
