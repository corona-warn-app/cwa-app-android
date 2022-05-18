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
interface InitializerModule {

    @Binds
    @IntoSet
    fun watchdogService(initializer: WatchdogService): Initializer

    @Binds
    @IntoSet
    fun configChangeDetector(initializer: ConfigChangeDetector): Initializer

    @Binds
    @IntoSet
    fun ewRiskLevelChangeDetector(initializer: EwRiskLevelChangeDetector): Initializer

    @Binds
    @IntoSet
    fun combinedRiskLevelChangeDetector(initializer: CombinedRiskLevelChangeDetector): Initializer

    @Binds
    @IntoSet
    fun deadmanNotificationScheduler(initializer: DeadmanNotificationScheduler): Initializer

    @Binds
    @IntoSet
    fun dataDonationAnalyticsScheduler(initializer: DataDonationAnalyticsScheduler): Initializer

    @Binds
    @IntoSet
    fun deviceTimeHandler(initializer: DeviceTimeHandler): Initializer

    @Binds
    @IntoSet
    fun autoSubmission(initializer: AutoSubmission): Initializer

    @Binds
    @IntoSet
    fun autoCheckOut(initializer: AutoCheckOut): Initializer

    @Binds
    @IntoSet
    fun traceLocationDbCleanUpScheduler(initializer: TraceLocationDbCleanUpScheduler): Initializer

    @Binds
    @IntoSet
    fun shareTestResultNotificationService(initializer: ShareTestResultNotificationService): Initializer

    @Binds
    @IntoSet
    fun exposureWindowRiskWorkScheduler(initializer: ExposureWindowRiskWorkScheduler): Initializer

    @Binds
    @IntoSet
    fun presenceTracingRiskWorkScheduler(initializer: PresenceTracingRiskWorkScheduler): Initializer

    @Binds
    @IntoSet
    fun pcrResultScheduler(initializer: PCRResultScheduler): Initializer

    @Binds
    @IntoSet
    fun raResultScheduler(initializer: RAResultScheduler): Initializer

    @Binds
    @IntoSet
    fun pcrTestResultAvailableNotificationService(initializer: PCRTestResultAvailableNotificationService): Initializer

    @Binds
    @IntoSet
    fun ratTestResultAvailableNotificationService(initializer: RATTestResultAvailableNotificationService): Initializer

    @Binds
    @IntoSet
    fun testCertificateRetrievalScheduler(initializer: TestCertificateRetrievalScheduler): Initializer

    @Binds
    @IntoSet
    fun environmentSetup(initializer: EnvironmentSetup): Initializer

    @Binds
    @IntoSet
    fun localStatisticsRetrievalScheduler(initializer: LocalStatisticsRetrievalScheduler): Initializer

    @Binds
    @IntoSet
    fun dccStateCheckScheduler(initializer: DccStateCheckScheduler): Initializer

    @Binds
    @IntoSet
    fun securityProvider(initializer: SecurityProvider): Initializer

    @Binds
    @IntoSet
    fun recycleBinCleanUpScheduler(initializer: RecycleBinCleanUpScheduler): Initializer

    @Binds
    @IntoSet
    fun cclConfigurationUpdateScheduler(initializer: CclConfigurationUpdateScheduler): Initializer

    @Binds
    @IntoSet
    fun familyTestResultRetrievalScheduler(initializer: FamilyTestResultRetrievalScheduler): Initializer

    @Binds
    @IntoSet
    fun dccValidityStateChangeObserver(initializer: DccValidityStateChangeObserver): Initializer

    @Binds
    @IntoSet
    fun dccRevocationUpdateScheduler(initializer: DccRevocationUpdateScheduler): Initializer

    @Binds
    @IntoSet
    fun contactDiaryWorkScheduler(initializer: ContactDiaryWorkScheduler): Initializer
}
