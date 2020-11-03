package de.rki.coronawarnapp.storage

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ExposureSummaryRepository(private val exposureSummaryDao: ExposureSummaryDao) {
    companion object {
        @Volatile
        private var instance: ExposureSummaryRepository? = null

        private fun getInstance(exposureSummaryDao: ExposureSummaryDao) =
            instance ?: synchronized(this) {
                instance ?: ExposureSummaryRepository(exposureSummaryDao).also { instance = it }
            }

        fun resetInstance() = synchronized(this) {
            instance = null
        }

        fun getExposureSummaryRepository(): ExposureSummaryRepository {
            return getInstance(
                AppDatabase.getInstance(CoronaWarnApplication.getAppContext())
                    .exposureSummaryDao()
            )
        }

        private val internalMatchedKeyCount = MutableStateFlow(0)
        val matchedKeyCount: Flow<Int> = internalMatchedKeyCount

        private val internalDaysSinceLastExposure = MutableStateFlow(0)
        val daysSinceLastExposure: Flow<Int> = internalDaysSinceLastExposure
    }

    suspend fun getExposureSummaryEntities() = exposureSummaryDao.getExposureSummaryEntities()
        .map { it.convertToExposureSummary() }

    suspend fun insertExposureSummaryEntity(exposureSummary: ExposureSummary) =
        ExposureSummaryEntity().apply {
            this.daysSinceLastExposure = exposureSummary.daysSinceLastExposure
            this.matchedKeyCount = exposureSummary.matchedKeyCount
            this.maximumRiskScore = exposureSummary.maximumRiskScore
            this.summationRiskScore = exposureSummary.summationRiskScore
            this.attenuationDurationsInMinutes =
                exposureSummary.attenuationDurationsInMinutes.toTypedArray().toList()
        }.run {
            exposureSummaryDao.insertExposureSummaryEntity(this)
            internalMatchedKeyCount.value = matchedKeyCount
            internalDaysSinceLastExposure.value = daysSinceLastExposure
        }

    suspend fun getLatestExposureSummary(token: String) {
        if (InternalExposureNotificationClient.asyncIsEnabled())
            InternalExposureNotificationClient.asyncGetExposureSummary(token).also {
                internalMatchedKeyCount.value = it.matchedKeyCount
                internalDaysSinceLastExposure.value = it.daysSinceLastExposure
            }
    }

    private fun ExposureSummaryEntity.convertToExposureSummary() =
        ExposureSummary.ExposureSummaryBuilder()
            .setAttenuationDurations(this.attenuationDurationsInMinutes.toIntArray())
            .setDaysSinceLastExposure(this.daysSinceLastExposure)
            .setMatchedKeyCount(this.matchedKeyCount)
            .setMaximumRiskScore(this.maximumRiskScore)
            .setSummationRiskScore(this.summationRiskScore)
            .build()
}
