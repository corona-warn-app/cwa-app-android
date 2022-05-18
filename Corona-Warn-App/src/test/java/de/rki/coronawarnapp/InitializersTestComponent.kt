package de.rki.coronawarnapp

import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.ConfigChangeDetector
import de.rki.coronawarnapp.appconfig.devicetime.DeviceTimeHandler
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdateScheduler
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler
import de.rki.coronawarnapp.coronatest.type.rapidantigen.notification.RATTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateCheckScheduler
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateChangeObserver
import de.rki.coronawarnapp.covidcertificate.revocation.update.DccRevocationUpdateScheduler
import de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalScheduler
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.familytest.worker.FamilyTestResultRetrievalScheduler
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.initializer.InitializerModule
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpScheduler
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import de.rki.coronawarnapp.risk.changedetection.CombinedRiskLevelChangeDetector
import de.rki.coronawarnapp.risk.changedetection.EwRiskLevelChangeDetector
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsRetrievalScheduler
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.WatchdogService
import io.mockk.mockk
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        InitializerModule::class,
        MockInitializerProvider::class
    ]
)
interface InitializersTestComponent {

    val initializers: Set<@JvmSuppressWildcards Initializer>

    @Component.Factory

    interface Factory {
        fun create(): InitializersTestComponent
    }
}

@Module
class MockInitializerProvider {

    @Provides
    fun watchdogService() = mockk<WatchdogService>(relaxed = true)

    @Provides
    fun configChangeDetector() = mockk<ConfigChangeDetector>(relaxed = true)

    @Provides
    fun ewRiskLevelChangeDetector() = mockk<EwRiskLevelChangeDetector>(relaxed = true)

    @Provides
    fun combinedRiskLevelChangeDetector() = mockk<CombinedRiskLevelChangeDetector>(relaxed = true)

    @Provides
    fun deadmanNotificationScheduler() = mockk<DeadmanNotificationScheduler>(relaxed = true)

    @Provides
    fun dataDonationAnalyticsScheduler() = mockk<DataDonationAnalyticsScheduler>(relaxed = true)

    @Provides
    fun deviceTimeHandler() = mockk<DeviceTimeHandler>(relaxed = true)

    @Provides
    fun autoSubmission() = mockk<AutoSubmission>(relaxed = true)

    @Provides
    fun autoCheckOut() = mockk<AutoCheckOut>(relaxed = true)

    @Provides
    fun traceLocationDbCleanUpScheduler() = mockk<TraceLocationDbCleanUpScheduler>(relaxed = true)

    @Provides
    fun shareTestResultNotificationService() = mockk<ShareTestResultNotificationService>(relaxed = true)

    @Provides
    fun exposureWindowRiskWorkScheduler() = mockk<ExposureWindowRiskWorkScheduler>(relaxed = true)

    @Provides
    fun presenceTracingRiskWorkScheduler() = mockk<PresenceTracingRiskWorkScheduler>(relaxed = true)

    @Provides
    fun pcrResultScheduler() = mockk<PCRResultScheduler>(relaxed = true)

    @Provides
    fun raResultScheduler() = mockk<RAResultScheduler>(relaxed = true)

    @Provides
    fun pcrTestResultAvailableNotificationService() = mockk<PCRTestResultAvailableNotificationService>(relaxed = true)

    @Provides
    fun ratTestResultAvailableNotificationService() = mockk<RATTestResultAvailableNotificationService>(relaxed = true)

    @Provides
    fun testCertificateRetrievalScheduler() = mockk<TestCertificateRetrievalScheduler>(relaxed = true)

    @Provides
    fun environmentSetup() = mockk<EnvironmentSetup>(relaxed = true)

    @Provides
    fun localStatisticsRetrievalScheduler() = mockk<LocalStatisticsRetrievalScheduler>(relaxed = true)

    @Provides
    fun dccStateCheckScheduler() = mockk<DccStateCheckScheduler>(relaxed = true)

    @Provides
    fun securityProvider() = mockk<SecurityProvider>(relaxed = true)

    @Provides
    fun recycleBinCleanUpScheduler() = mockk<RecycleBinCleanUpScheduler>(relaxed = true)

    @Provides
    fun cclConfigurationUpdateScheduler() = mockk<CclConfigurationUpdateScheduler>(relaxed = true)

    @Provides
    fun familyTestResultRetrievalScheduler() = mockk<FamilyTestResultRetrievalScheduler>(relaxed = true)

    @Provides
    fun dccValidityStateChangeObserver() = mockk<DccValidityStateChangeObserver>(relaxed = true)

    @Provides
    fun dccRevocationUpdateScheduler() = mockk<DccRevocationUpdateScheduler>(relaxed = true)

    @Provides
    fun contactDiaryWorkScheduler() = mockk<ContactDiaryWorkScheduler>(relaxed = true)
}
