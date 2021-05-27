package de.rki.coronawarnapp.util

import android.annotation.SuppressLint
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.greencertificate.storage.CertificatesPreferences
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.vaccination.core.repository.ValueSetsRepository
import de.rki.coronawarnapp.vaccination.core.VaccinationPreferences
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
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
    private val keyCacheRepository: KeyCacheRepository,
    private val appConfigProvider: AppConfigProvider,
    private val submissionRepository: SubmissionRepository,
    private val exposureDetectionTracker: ExposureDetectionTracker,
    private val downloadDiagnosisKeysSettings: DownloadDiagnosisKeysSettings,
    private val riskLevelStorage: RiskLevelStorage,
    private val contactDiaryRepository: ContactDiaryRepository,
    private var contactDiaryPreferences: ContactDiaryPreferences,
    private var traceLocationPreferences: TraceLocationPreferences,
    private val cwaSettings: CWASettings,
    private val statisticsProvider: StatisticsProvider,
    private val surveySettings: SurveySettings,
    private val analyticsSettings: AnalyticsSettings,
    private val analytics: Analytics,
    private val bugReportingSettings: BugReportingSettings,
    private val tracingSettings: TracingSettings,
    private val onboardingSettings: OnboardingSettings,
    private val submissionSettings: SubmissionSettings,
    private val traceLocationRepository: TraceLocationRepository,
    private val checkInRepository: CheckInRepository,
    private val traceLocationSettings: TraceLocationSettings,
    private val traceWarningRepository: TraceWarningRepository,
    private val coronaTestRepository: CoronaTestRepository,
    private val ratProfileSettings: RATProfileSettings,
    private val valueSetsRepository: ValueSetsRepository,
    private val vaccinationPreferences: VaccinationPreferences,
    private val vaccinationRepository: VaccinationRepository,
    private val certificatesPreferences: CertificatesPreferences,
) {

    private val mutex = Mutex()

    /**
     * Deletes all data known to the Application
     *
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    suspend fun clearAllLocalData() = mutex.withLock {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
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
        traceLocationPreferences.clear()
        cwaSettings.clear()
        surveySettings.clear()
        analyticsSettings.clear()
        tracingSettings.clear()
        onboardingSettings.clear()
        submissionSettings.clear()
        traceLocationSettings.clear()

        // Clear contact diary database
        contactDiaryRepository.clear()

        statisticsProvider.clear()

        bugReportingSettings.clear()

        traceWarningRepository.clear()
        traceLocationRepository.deleteAllTraceLocations()
        checkInRepository.clear()
        coronaTestRepository.clear()
        ratProfileSettings.deleteProfile()

        valueSetsRepository.clear()
        vaccinationRepository.clear()
        vaccinationPreferences.clear()
        certificatesPreferences.clear()

        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }
}
