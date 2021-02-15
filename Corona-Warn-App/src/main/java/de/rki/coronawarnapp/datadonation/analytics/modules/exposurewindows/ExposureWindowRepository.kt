package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.util.TimeStamper
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

interface ExposureWindowRepository {
    suspend fun getNewContributions(): List<ExposureWindowContribution>
    suspend fun add(contribution: ExposureWindowContribution)
    suspend fun moveToReported(contributions: List<ExposureWindowContribution>)
}

class ExposureWindowStorage(
    private val dao: ExposureWindowContributionDao,
    val timeStamper: TimeStamper
) : ExposureWindowRepository {
    override suspend fun getNewContributions(): List<ExposureWindowContribution> {
        return dao.allNewEntries().map { it.asExposureWindowContribution }
    }

    override suspend fun add(contribution: ExposureWindowContribution) {
        contribution.toString().getSha256Hash()?.let {
            dao.insert(contribution.toWrapper(it))
        }
    }

    override suspend fun moveToReported(contributions: List<ExposureWindowContribution>) {
        val wrapperEntities = contributions.map { contribution ->
            contribution.toString().getSha256Hash()?.let { key ->
                contribution.toWrapper(key)
            }
        }.filterNotNull()
        dao.moveToReported(wrapperEntities, timeStamper.nowUTC.millis)
    }
}

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

private fun ExposureWindowContribution.toWrapper(key: String) = ExposureWindowWrapper(
    exposureWindow = ExposureWindowEntity(
        sha256Hash = key,
        calibrationConfidence = calibrationConfidence,
        dateMillis = dateMillis,
        infectiousness = infectiousness,
        reportType = reportType,
        normalizedTime = normalizedTime,
        transmissionRiskLevel = transmissionRiskLevel
    ),
    scanInstances = scanInstances.map { it.toEntity(key) }
)

private fun ScanInstanceContribution.toEntity(foreignKey: String) =
    ScanInstanceEntity(
        fkSha256Hash = foreignKey,
        minAttenuation = minAttenuation,
        typicalAttenuation = typicalAttenuation,
        secondsSinceLastScan = secondsSinceLastScan
    )
