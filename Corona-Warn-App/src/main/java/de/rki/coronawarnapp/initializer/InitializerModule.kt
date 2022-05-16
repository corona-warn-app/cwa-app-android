package de.rki.coronawarnapp.initializer

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.SecurityProvider
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

@Suppress("TooManyFunctions")
@Module
class InitializerModule {

    @Binds
    @IntoSet
    fun watchdogService(initializer: WatchdogService) = initializer

    @Binds
    @IntoSet
    fun configChangeDetector(initializer: ConfigChangeDetector) = initializer

    @Binds
    @IntoSet
    fun ewRiskLevelChangeDetector(initializer: EwRiskLevelChangeDetector) = initializer

    @Binds
    @IntoSet
    fun combinedRiskLevelChangeDetector(initializer: CombinedRiskLevelChangeDetector) = initializer

    @Binds
    @IntoSet
    fun deadmanNotificationScheduler(initializer: DeadmanNotificationScheduler) = initializer

    @Binds
    @IntoSet
    fun dataDonationAnalyticsScheduler(initializer: DataDonationAnalyticsScheduler) = initializer

    @Binds
    @IntoSet
    fun deviceTimeHandler(initializer: DeviceTimeHandler) = initializer

    @Binds
    @IntoSet
    fun autoSubmission(initializer: AutoSubmission) = initializer

    @Binds
    @IntoSet
    fun autoCheckOut(initializer: AutoCheckOut) = initializer

    @Binds
    @IntoSet
    fun traceLocationDbCleanUpScheduler(initializer: TraceLocationDbCleanUpScheduler) = initializer

    @Binds
    @IntoSet
    fun shareTestResultNotificationService(initializer: ShareTestResultNotificationService) = initializer

    @Binds
    @IntoSet
    fun exposureWindowRiskWorkScheduler(initializer: ExposureWindowRiskWorkScheduler) = initializer

    @Binds
    @IntoSet
    fun presenceTracingRiskWorkScheduler(initializer: PresenceTracingRiskWorkScheduler) = initializer

    @Binds
    @IntoSet
    fun pcrResultScheduler(initializer: PCRResultScheduler) = initializer

    @Binds
    @IntoSet
    fun raResultScheduler(initializer: RAResultScheduler) = initializer

    @Binds
    @IntoSet
    fun pcrTestResultAvailableNotificationService(initializer: PCRTestResultAvailableNotificationService) = initializer

    @Binds
    @IntoSet
    fun ratTestResultAvailableNotificationService(initializer: RATTestResultAvailableNotificationService) = initializer

    @Binds
    @IntoSet
    fun testCertificateRetrievalScheduler(initializer: TestCertificateRetrievalScheduler) = initializer

    @Binds
    @IntoSet
    fun environmentSetup(initializer: EnvironmentSetup) = initializer

    @Binds
    @IntoSet
    fun localStatisticsRetrievalScheduler(initializer: LocalStatisticsRetrievalScheduler) = initializer

    @Binds
    @IntoSet
    fun dccStateCheckScheduler(initializer: DccStateCheckScheduler) = initializer

    @Binds
    @IntoSet
    fun securityProvider(initializer: SecurityProvider) = initializer

    @Binds
    @IntoSet
    fun recycleBinCleanUpScheduler(initializer: RecycleBinCleanUpScheduler) = initializer

    @Binds
    @IntoSet
    fun cclConfigurationUpdateScheduler(initializer: CclConfigurationUpdateScheduler) = initializer

    @Binds
    @IntoSet
    fun familyTestResultRetrievalScheduler(initializer: FamilyTestResultRetrievalScheduler) = initializer

    @Binds
    @IntoSet
    fun dccValidityStateChangeObserver(initializer: DccValidityStateChangeObserver) = initializer

    @Binds
    @IntoSet
    fun dccRevocationUpdateScheduler(initializer: DccRevocationUpdateScheduler) = initializer

    @Binds
    @IntoSet
    fun contactDiaryWorkScheduler(initializer: ContactDiaryWorkScheduler) = initializer
}
