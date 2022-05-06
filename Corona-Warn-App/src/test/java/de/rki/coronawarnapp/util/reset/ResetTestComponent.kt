package de.rki.coronawarnapp.util.reset

import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.AppConfigModule
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.ccl.CclModule
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.configuration.storage.DownloadedCclConfigurationStorage
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryStorageModule
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestModule
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.covidcertificate.DigitalCovidCertificateModule
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationModule
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationReset
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationCache
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetServer
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsModule
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.survey.SurveyModule
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.dccticketing.core.DccTicketingCoreModule
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.familytest.core.FamilyTestModule
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.main.MainModule
import de.rki.coronawarnapp.nearby.NearbyModule
import de.rki.coronawarnapp.nearby.modules.detectiontracker.DefaultExposureDetectionTracker
import de.rki.coronawarnapp.presencetracing.PresenceTracingModule
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.profile.ProfileModule
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.statistics.StatisticsModule
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsCache
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsServer
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.source.StatisticsCache
import de.rki.coronawarnapp.statistics.source.StatisticsServer
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.StorageModule
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import io.mockk.mockk
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MockProvider::class,
        AppConfigModule.ResetModule::class,
        BugReportingSharedModule.ResetModule::class,
        CclModule.ResetModule::class,
        ContactDiaryStorageModule.ResetModule::class,
        CoronaTestModule.ResetModule::class,
        ProfileModule.ResetModule::class,
        DigitalCovidCertificateModule.ResetModule::class,
        CertificateValueSetModule.ResetModule::class,
        DccValidationModule.ResetModule::class,
        DccRevocationModule.ResetModule::class,
        SurveyModule.ResetModule::class,
        AnalyticsModule.ResetModule::class,
        DccTicketingCoreModule.ResetModule::class,
        DiagnosisKeysModule.ResetModule::class,
        FamilyTestModule.ResetModule::class,
        MainModule::class,
        NearbyModule.ResetModule::class,
        PresenceTracingModule.ResetModule::class,
        PresenceTracingWarningModule.ResetModule::class,
        RiskModule.ResetModule::class,
        StatisticsModule.ResetModule::class,
        StorageModule::class,
        SubmissionModule.ResetModule::class
    ]
)
interface ResetTestComponent {

    val resettableSet: Set<Resettable>

    @Component.Factory
    interface Factory {
        fun create(): ResetTestComponent
    }
}

@Module
object MockProvider {

    @Provides
    fun provideAppConfigProvider(): AppConfigProvider = mockk(relaxed = true)

    @Provides
    fun provideBugReportingSettings(): BugReportingSettings = mockk(relaxed = true)

    @Provides
    fun provideCclSettings(): CclSettings = mockk(relaxed = true)

    @Provides
    fun provideDownloadedCclConfigurationStorage(): DownloadedCclConfigurationStorage = mockk(relaxed = true)

    @Provides
    fun provideCclConfigurationRepository(): CclConfigurationRepository = mockk(relaxed = true)

    @Provides
    fun provideDccWalletInfoRepository(): DccWalletInfoRepository = mockk(relaxed = true)

    @Provides
    fun provideContactDiaryPreferences(): ContactDiaryPreferences = mockk(relaxed = true)

    @Provides
    fun provideDefaultContactDiaryRepository(): DefaultContactDiaryRepository = mockk(relaxed = true)

    @Provides
    fun provideProfileSettingsDataStore(): ProfileSettingsDataStore = mockk(relaxed = true)

    @Provides
    fun provideProfileRepository(): ProfileRepository = mockk(relaxed = true)

    @Provides
    fun provideCoronaTestRepository(): CoronaTestRepository = mockk(relaxed = true)

    @Provides
    fun provideDscRepository(): DscRepository = mockk(relaxed = true)

    @Provides
    fun provideVaccinationCertificateRepository(): VaccinationCertificateRepository = mockk(relaxed = true)

    @Provides
    fun provideTestCertificateRepository(): TestCertificateRepository = mockk(relaxed = true)

    @Provides
    fun provideRecoveryCertificateRepository(): RecoveryCertificateRepository = mockk(relaxed = true)

    @Provides
    fun provideBoosterRulesRepository(): BoosterRulesRepository = mockk(relaxed = true)

    @Provides
    fun providePersonCertificatesSettings(): PersonCertificatesSettings = mockk(relaxed = true)

    @Provides
    fun provideCovidCertificateSettings(): CovidCertificateSettings = mockk(relaxed = true)

    @Provides
    fun provideValueSetsRepository(): ValueSetsRepository = mockk(relaxed = true)

    @Provides
    fun provideCertificateValueSetServer(): CertificateValueSetServer = mockk(relaxed = true)

    @Provides
    fun provideDccValidationServer(): DccValidationServer = mockk(relaxed = true)

    @Provides
    fun provideDccValidationCache(): DccValidationCache = mockk(relaxed = true)

    @Provides
    fun provideDccRevocationReset(): DccRevocationReset = mockk(relaxed = true)

    @Provides
    fun provideSurveySettings(): SurveySettings = mockk(relaxed = true)

    @Provides
    fun provideAnalytics(): Analytics = mockk(relaxed = true)

    @Provides
    fun provideAnalyticsSettings(): AnalyticsSettings = mockk(relaxed = true)

    @Provides
    fun provideAnalyticsExposureWindowsSettings(): AnalyticsExposureWindowsSettings = mockk(relaxed = true)

    @Provides
    fun provideDccTicketingAllowListRepository(): DccTicketingAllowListRepository = mockk(relaxed = true)

    @Provides
    fun provideDccTicketingAllowListStorage(): DccTicketingAllowListStorage = mockk(relaxed = true)

    @Provides
    fun provideDccTicketingQrCodeSettings(): DccTicketingQrCodeSettings = mockk(relaxed = true)

    @Provides
    fun provideKeyCacheRepository(): KeyCacheRepository = mockk(relaxed = true)

    @Provides
    fun provideDownloadDiagnosisKeysSettings(): DownloadDiagnosisKeysSettings = mockk(relaxed = true)

    @Provides
    fun provideFamilyTestStorage(): FamilyTestStorage = mockk(relaxed = true)

    @Provides
    fun provideCWASettings(): CWASettings = mockk(relaxed = true)

    @Provides
    fun provideTraceWarningRepository(): TraceWarningRepository = mockk(relaxed = true)

    @Provides
    fun provideTraceLocationPreferences(): TraceLocationPreferences = mockk(relaxed = true)

    @Provides
    fun provideTraceLocationSettings(): TraceLocationSettings = mockk(relaxed = true)

    @Provides
    fun provideCheckInRepository(): CheckInRepository = mockk(relaxed = true)

    @Provides
    fun provideDefaultTraceLocationRepository(): DefaultTraceLocationRepository = mockk(relaxed = true)

    @Provides
    fun provideDefaultRiskLevelStorage(): DefaultRiskLevelStorage = mockk(relaxed = true)

    @Provides
    fun provideStatisticsCache(): StatisticsCache = mockk(relaxed = true)

    @Provides
    fun provideStatisticsServer(): StatisticsServer = mockk(relaxed = true)

    @Provides
    fun provideLocalStatisticsConfigStorage(): LocalStatisticsConfigStorage = mockk(relaxed = true)

    @Provides
    fun provideLocalStatisticsServer(): LocalStatisticsServer = mockk(relaxed = true)

    @Provides
    fun provideLocalStatisticsCache(): LocalStatisticsCache = mockk(relaxed = true)

    @Provides
    fun provideOnboardingSettings(): OnboardingSettings = mockk(relaxed = true)

    @Provides
    fun provideTracingSettings(): TracingSettings = mockk(relaxed = true)

    @Provides
    fun provideSubmissionSettings(): SubmissionSettings = mockk(relaxed = true)

    @Provides
    fun provideTEKHistoryStorage(): TEKHistoryStorage = mockk(relaxed = true)

    @Provides
    fun provideDefaultExposureDetectionTracker(): DefaultExposureDetectionTracker = mockk(relaxed = true)
}
