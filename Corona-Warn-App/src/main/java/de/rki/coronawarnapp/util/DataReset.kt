package de.rki.coronawarnapp.util

import android.annotation.SuppressLint
import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for supplying functionality regarding Data Retention
 */
@Suppress("LongParameterList")
@Singleton
class DataReset @Inject constructor(
    @AppContext private val context: Context,
    private val keyCacheRepository: KeyCacheRepository,
    private val appConfigProvider: AppConfigProvider,
    private val submissionRepository: SubmissionRepository,
    private val exposureDetectionTracker: ExposureDetectionTracker,
    private val downloadDiagnosisKeysSettings: DownloadDiagnosisKeysSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val contactDiaryRepository: ContactDiaryRepository,
    private var contactDiaryPreferences: ContactDiaryPreferences,
    private val cwaSettings: CWASettings,
    private val statisticsProvider: StatisticsProvider,
    private val surveySettings: SurveySettings,
    private val analyticsSettings: AnalyticsSettings,
    private val analytics: Analytics
) {

    private val mutex = Mutex()

    /**
     * Deletes all data known to the Application
     *
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    suspend fun clearAllLocalData() = mutex.withLock {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
        // Database Reset
        AppDatabase.reset(context)
        // Because LocalData does not behave like a normal shared preference
        LocalData.clear()
        // Shared Preferences Reset
        SecurityHelper.resetSharedPrefs()

        // Triggers deletion of all analytics contributed data
        analytics.setAnalyticsEnabled(false)

        // Reset the current states stored in LiveData
        submissionRepository.reset()
        keyCacheRepository.clear()
        appConfigProvider.clear()
        exposureDetectionTracker.clear()
        downloadDiagnosisKeysSettings.clear()
        riskLevelStorage.clear()
        contactDiaryPreferences.clear()
        cwaSettings.clear()
        surveySettings.clear()
        analyticsSettings.clear()

        // Clear contact diary database
        contactDiaryRepository.clear()

        statisticsProvider.clear()

        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }
}
