package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.CoronaWarnApplication

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

        val matchedKeyCount = MutableLiveData<Int>()
        val daysSinceLastExposure = MutableLiveData<Int>()
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
            ExposureSummaryRepository.matchedKeyCount.postValue(matchedKeyCount)
            ExposureSummaryRepository.daysSinceLastExposure.postValue(daysSinceLastExposure)
        }

    suspend fun getLatestExposureSummary() = exposureSummaryDao
        .getLatestExposureSummary()
        ?.convertToExposureSummary()
        .also {
            matchedKeyCount.postValue(it?.matchedKeyCount)
            daysSinceLastExposure.postValue(it?.daysSinceLastExposure)
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
