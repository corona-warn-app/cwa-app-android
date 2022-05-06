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
import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Singleton

class ResetInjectionTest : BaseTest() {

    /**
     * Scan our class graph, and compare it against what we inject via Dagger.
     * Check if an [Resettable] was accidentally forgotten.
     */
    @Test
    fun `all resettable are injected`() {
        val resettableSet = DaggerResetTestComponent.create().resettableSet

        println("We know ${resettableSet.size} resettable")
        resettableSet.isNotEmpty() shouldBe true

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val resettableClasses = scanResult
            .getClassesImplementing(Resettable::class.java)
            .filterNot { it.isAbstract || it.isAnonymousInnerClass }

        println("Our project contains ${resettableClasses.size} resettable classes")
        val injected = resettableSet.map { it::class.java.simpleName }.toSet()
        val existing = resettableClasses.map { it.simpleName }.toSet()
        injected shouldContainAll existing
    }
}

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
    fun provideAppConfigProvider(): AppConfigProvider = mockk()

    @Provides
    fun provideBugReportingSettings(): BugReportingSettings = mockk()

    @Provides
    fun provideCclSettings(): CclSettings = mockk()

    @Provides
    fun provideDownloadedCclConfigurationStorage(): DownloadedCclConfigurationStorage = mockk()

    @Provides
    fun provideCclConfigurationRepository(): CclConfigurationRepository = mockk()

    @Provides
    fun provideDccWalletInfoRepository(): DccWalletInfoRepository = mockk()

    @Provides
    fun provideContactDiaryPreferences(): ContactDiaryPreferences = mockk()

    @Provides
    fun provideDefaultContactDiaryRepository(): DefaultContactDiaryRepository = mockk()

    @Provides
    fun provideProfileSettingsDataStore(): ProfileSettingsDataStore = mockk()

    @Provides
    fun provideProfileRepository(): ProfileRepository = mockk()

    @Provides
    fun provideCoronaTestRepository(): CoronaTestRepository = mockk()

    @Provides
    fun provideDscRepository(): DscRepository = mockk()

    @Provides
    fun provideVaccinationCertificateRepository(): VaccinationCertificateRepository = mockk()

    @Provides
    fun provideTestCertificateRepository(): TestCertificateRepository = mockk()

    @Provides
    fun provideRecoveryCertificateRepository(): RecoveryCertificateRepository = mockk()

    @Provides
    fun provideBoosterRulesRepository(): BoosterRulesRepository = mockk()

    @Provides
    fun providePersonCertificatesSettings(): PersonCertificatesSettings = mockk()

    @Provides
    fun provideCovidCertificateSettings(): CovidCertificateSettings = mockk()

    @Provides
    fun provideValueSetsRepository(): ValueSetsRepository = mockk()

    @Provides
    fun provideCertificateValueSetServer(): CertificateValueSetServer = mockk()

    @Provides
    fun provideDccValidationServer(): DccValidationServer = mockk()

    @Provides
    fun provideDccValidationCache(): DccValidationCache = mockk()

    @Provides
    fun provideDccRevocationReset(): DccRevocationReset = mockk()

    @Provides
    fun provideSurveySettings(): SurveySettings = mockk()

    @Provides
    fun provideAnalytics(): Analytics = mockk()

    @Provides
    fun provideAnalyticsSettings(): AnalyticsSettings = mockk()

    @Provides
    fun provideAnalyticsExposureWindowsSettings(): AnalyticsExposureWindowsSettings = mockk()

    @Provides
    fun provideDccTicketingAllowListRepository(): DccTicketingAllowListRepository = mockk()

    @Provides
    fun provideDccTicketingAllowListStorage(): DccTicketingAllowListStorage = mockk()

    @Provides
    fun provideDccTicketingQrCodeSettings(): DccTicketingQrCodeSettings = mockk()

    @Provides
    fun provideKeyCacheRepository(): KeyCacheRepository = mockk()

    @Provides
    fun provideDownloadDiagnosisKeysSettings(): DownloadDiagnosisKeysSettings = mockk()

    @Provides
    fun provideFamilyTestStorage(): FamilyTestStorage = mockk()

    @Provides
    fun provideCWASettings(): CWASettings = mockk()

    @Provides
    fun provideTraceWarningRepository(): TraceWarningRepository = mockk()

    @Provides
    fun provideTraceLocationPreferences(): TraceLocationPreferences = mockk()

    @Provides
    fun provideTraceLocationSettings(): TraceLocationSettings = mockk()

    @Provides
    fun provideCheckInRepository(): CheckInRepository = mockk()

    @Provides
    fun provideDefaultTraceLocationRepository(): DefaultTraceLocationRepository = mockk()

    @Provides
    fun provideDefaultRiskLevelStorage(): DefaultRiskLevelStorage = mockk()

    @Provides
    fun provideStatisticsCache(): StatisticsCache = mockk()

    @Provides
    fun provideStatisticsServer(): StatisticsServer = mockk()

    @Provides
    fun provideLocalStatisticsConfigStorage(): LocalStatisticsConfigStorage = mockk()

    @Provides
    fun provideLocalStatisticsServer(): LocalStatisticsServer = mockk()

    @Provides
    fun provideLocalStatisticsCache(): LocalStatisticsCache = mockk()

    @Provides
    fun provideOnboardingSettings(): OnboardingSettings = mockk()

    @Provides
    fun provideTracingSettings(): TracingSettings = mockk()

    @Provides
    fun provideSubmissionSettings(): SubmissionSettings = mockk()

    @Provides
    fun provideTEKHistoryStorage(): TEKHistoryStorage = mockk()

    @Provides
    fun provideDefaultExposureDetectionTracker(): DefaultExposureDetectionTracker = mockk()
}
