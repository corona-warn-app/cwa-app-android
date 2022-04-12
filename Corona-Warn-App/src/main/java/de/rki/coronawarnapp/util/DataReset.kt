package de.rki.coronawarnapp.util

import android.annotation.SuppressLint
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsProvider
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
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
    private val localStatisticsProvider: LocalStatisticsProvider,
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
    private val ratProfileSettings: RATProfileSettingsDataStore,
    private val valueSetsRepository: ValueSetsRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val vaccinationCertificateRepository: VaccinationCertificateRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val personCertificatesSettings: PersonCertificatesSettings,
    private val validationRepository: DccValidationRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dscRepository: DscRepository,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val exposureWindowsSettings: AnalyticsExposureWindowsSettings,
    private val dccTicketingAllowListRepository: DccTicketingAllowListRepository,
    private val dccTicketingQrCodeSettings: DccTicketingQrCodeSettings,
    private val cclConfigurationRepository: CclConfigurationRepository,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val cclSettings: CclSettings,
    private val familyTestRepository: FamilyTestRepository,
    private val revocationRepository: RevocationRepository
) {

    private val mutex = Mutex()

    /**
     * Deletes all data known to the Application
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    suspend fun clearAllLocalData() = mutex.withLock {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
        // Triggers deletion of all analytics contributed data
        analytics.setAnalyticsEnabled(false)
        exposureWindowsSettings.clear()

        // Reset the current states stored in LiveData
        submissionRepository.reset()
        keyCacheRepository.clear()
        appConfigProvider.clear()
        dscRepository.clear()
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
        localStatisticsProvider.clear()

        bugReportingSettings.clear()

        traceWarningRepository.clear()
        traceLocationRepository.deleteAllTraceLocations()
        checkInRepository.clear()
        coronaTestRepository.clear()
        ratProfileSettings.clear()

        valueSetsRepository.clear()

        vaccinationCertificateRepository.clear()
        testCertificateRepository.clear()
        recoveryCertificateRepository.clear()

        covidCertificateSettings.clear()
        personCertificatesSettings.clear()

        validationRepository.clear()

        boosterRulesRepository.clear()

        dccTicketingAllowListRepository.clear()

        dccTicketingQrCodeSettings.clear()

        cclSettings.clear()
        cclConfigurationRepository.clear()

        dccWalletInfoRepository.clear()

        familyTestRepository.clear()

        revocationRepository.clear()

        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }
}
