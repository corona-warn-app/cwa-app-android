package de.rki.coronawarnapp.util

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
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationReset
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
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DataResetTest : BaseTest() {

    @MockK lateinit var keyCacheRepository: KeyCacheRepository
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var exposureDetectionTracker: ExposureDetectionTracker
    @MockK lateinit var downloadDiagnosisKeysSettings: DownloadDiagnosisKeysSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var contactDiaryPreferences: ContactDiaryPreferences
    @MockK lateinit var traceLocationPreferences: TraceLocationPreferences
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var localStatisticsProvider: LocalStatisticsProvider
    @MockK lateinit var surveySettings: SurveySettings
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var analytics: Analytics
    @MockK lateinit var bugReportingSettings: BugReportingSettings
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var traceWarningRepository: TraceWarningRepository
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var ratProfileSettings: RATProfileSettingsDataStore
    @MockK lateinit var vaccinationCertificateRepository: VaccinationCertificateRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings
    @MockK lateinit var validationRepository: DccValidationRepository
    @MockK lateinit var recoveryCertificateRepository: RecoveryCertificateRepository
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK lateinit var exposureWindowsSettings: AnalyticsExposureWindowsSettings
    @MockK lateinit var dccTicketingAllowListRepository: DccTicketingAllowListRepository
    @MockK lateinit var dccTicketingQrCodeSettings: DccTicketingQrCodeSettings
    @MockK lateinit var cclConfigurationRepository: CclConfigurationRepository
    @MockK lateinit var cclSettings: CclSettings
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var dccRevocationReset: DccRevocationReset

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    fun createInstance() = DataReset(
        keyCacheRepository = keyCacheRepository,
        appConfigProvider = appConfigProvider,
        submissionRepository = submissionRepository,
        exposureDetectionTracker = exposureDetectionTracker,
        downloadDiagnosisKeysSettings = downloadDiagnosisKeysSettings,
        riskLevelStorage = riskLevelStorage,
        contactDiaryRepository = contactDiaryRepository,
        traceLocationPreferences = traceLocationPreferences,
        contactDiaryPreferences = contactDiaryPreferences,
        cwaSettings = cwaSettings,
        statisticsProvider = statisticsProvider,
        localStatisticsProvider = localStatisticsProvider,
        surveySettings = surveySettings,
        analyticsSettings = analyticsSettings,
        analytics = analytics,
        bugReportingSettings = bugReportingSettings,
        tracingSettings = tracingSettings,
        onboardingSettings = onboardingSettings,
        submissionSettings = submissionSettings,
        traceLocationRepository = traceLocationRepository,
        checkInRepository = checkInRepository,
        traceLocationSettings = traceLocationSettings,
        traceWarningRepository = traceWarningRepository,
        coronaTestRepository = coronaTestRepository,
        ratProfileSettings = ratProfileSettings,
        covidCertificateSettings = covidCertificateSettings,
        vaccinationCertificateRepository = vaccinationCertificateRepository,
        valueSetsRepository = valueSetsRepository,
        testCertificateRepository = testCertificateRepository,
        personCertificatesSettings = personCertificatesSettings,
        validationRepository = validationRepository,
        recoveryCertificateRepository = recoveryCertificateRepository,
        dscRepository = dscRepository,
        boosterRulesRepository = boosterRulesRepository,
        exposureWindowsSettings = exposureWindowsSettings,
        dccTicketingAllowListRepository = dccTicketingAllowListRepository,
        dccTicketingQrCodeSettings = dccTicketingQrCodeSettings,
        cclConfigurationRepository = cclConfigurationRepository,
        dccWalletInfoRepository = dccWalletInfoRepository,
        cclSettings = cclSettings,
        familyTestRepository = familyTestRepository,
        revocationReset = dccRevocationReset,
    )

    @Test
    fun `clearAllLocalData() should clear all data`() = runBlockingTest {
        createInstance().clearAllLocalData()

        coVerify(exactly = 1) {
            analytics.setAnalyticsEnabled(false)
            exposureWindowsSettings.clear()
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
            contactDiaryRepository.clear()
            statisticsProvider.clear()
            localStatisticsProvider.clear()
            bugReportingSettings.clear()
            traceWarningRepository.clear()
            traceLocationRepository.deleteAllTraceLocations()
            checkInRepository.clear()
            coronaTestRepository.clear()
            ratProfileSettings.clear()
            vaccinationCertificateRepository.clear()
            covidCertificateSettings.clear()
            valueSetsRepository.clear()
            personCertificatesSettings.clear()
            validationRepository.clear()
            recoveryCertificateRepository.clear()
            dscRepository.clear()
            boosterRulesRepository.clear()
            dccTicketingAllowListRepository.clear()
            dccTicketingQrCodeSettings.clear()
            cclSettings.clear()
            cclConfigurationRepository.clear()
            dccWalletInfoRepository.clear()
            familyTestRepository.clear()
            dccRevocationReset.clear()
        }
    }
}
