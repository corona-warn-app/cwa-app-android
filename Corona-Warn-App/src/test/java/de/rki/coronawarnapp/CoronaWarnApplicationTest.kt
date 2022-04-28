package de.rki.coronawarnapp

import androidx.work.WorkManager
import coil.ImageLoaderFactory
import dagger.android.DispatchingAndroidInjector
import de.rki.coronawarnapp.appconfig.ConfigChangeDetector
import de.rki.coronawarnapp.appconfig.devicetime.DeviceTimeHandler
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdateScheduler
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler
import de.rki.coronawarnapp.coronatest.type.rapidantigen.notification.RATTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateCheckScheduler
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateChangeObserver
import de.rki.coronawarnapp.covidcertificate.revocation.update.DccRevocationUpdateScheduler
import de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalScheduler
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.familytest.worker.FamilyTestResultRetrievalScheduler
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpScheduler
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import de.rki.coronawarnapp.risk.changedetection.CombinedRiskLevelChangeDetector
import de.rki.coronawarnapp.risk.changedetection.EwRiskLevelChangeDetector
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsRetrievalScheduler
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.WatchdogService
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.conscrypt.Conscrypt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.security.Security

class CoronaWarnApplicationTest : BaseTest() {

    @MockK lateinit var applicationComponent: ApplicationComponent
    @MockK lateinit var androidInjector: DispatchingAndroidInjector<Any>
    @MockK lateinit var watchdogService: WatchdogService
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var configChangeDetector: ConfigChangeDetector
    @MockK lateinit var ewRiskLevelChangeDetector: EwRiskLevelChangeDetector
    @MockK lateinit var combinedRiskLevelChangeDetector: CombinedRiskLevelChangeDetector
    @MockK lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @MockK lateinit var contactDiaryWorkScheduler: ContactDiaryWorkScheduler
    @MockK lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler
    @MockK lateinit var notificationHelper: GeneralNotifications
    @MockK lateinit var deviceTimeHandler: DeviceTimeHandler
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var autoCheckOut: AutoCheckOut
    @MockK lateinit var traceLocationDbCleanupScheduler: TraceLocationDbCleanUpScheduler
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler
    @MockK lateinit var presenceTracingRiskWorkScheduler: PresenceTracingRiskWorkScheduler
    @MockK lateinit var pcrTestResultScheduler: PCRResultScheduler
    @MockK lateinit var raTestResultScheduler: RAResultScheduler
    @MockK lateinit var testCertificateRetrievalScheduler: TestCertificateRetrievalScheduler
    @MockK lateinit var localStatisticsRetrievalScheduler: LocalStatisticsRetrievalScheduler
    @MockK lateinit var pcrTestResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var raTestResultAvailableNotificationService: RATTestResultAvailableNotificationService
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var imageLoaderFactory: ImageLoaderFactory
    @MockK lateinit var dscCheckScheduler: DccStateCheckScheduler
    @MockK lateinit var securityProvider: SecurityProvider
    @MockK lateinit var recycleBinCleanUpScheduler: RecycleBinCleanUpScheduler
    @MockK lateinit var vaccinationStorage: VaccinationStorage
    @MockK lateinit var cclConfigurationUpdateScheduler: CclConfigurationUpdateScheduler
    @MockK lateinit var familyTestResultRetrievalScheduler: FamilyTestResultRetrievalScheduler
    @MockK lateinit var dccValidityStateChangeObserver: DccValidityStateChangeObserver
    @MockK lateinit var dccRevocationUpdateScheduler: DccRevocationUpdateScheduler

    @ExperimentalCoroutinesApi
    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(Conscrypt::class)
        every { Conscrypt.newProvider() } returns mockk()

        mockkStatic(Security::class)
        every { Security.insertProviderAt(any(), any()) } returns 0

        mockkObject(CWADebug)
        CWADebug.apply {
            every { init(any()) } just Runs
            every { initAfterInjection(any()) } just Runs
        }

        mockkObject(AppInjector)
        AppInjector.apply {
            every { init(any()) } returns applicationComponent
        }
        applicationComponent.apply {
            every { inject(any<CoronaWarnApplication>()) } answers {
                val app = arg<CoronaWarnApplication>(0)
                app.component = applicationComponent
                app.androidInjector = androidInjector
                app.watchdogService = watchdogService
                app.taskController = taskController
                app.foregroundState = foregroundState
                app.workManager = workManager
                app.configChangeDetector = configChangeDetector
                app.ewRiskLevelChangeDetector = ewRiskLevelChangeDetector
                app.combinedRiskLevelChangeDetector = combinedRiskLevelChangeDetector
                app.deadmanNotificationScheduler = deadmanNotificationScheduler
                app.contactDiaryWorkScheduler = contactDiaryWorkScheduler
                app.dataDonationAnalyticsScheduler = dataDonationAnalyticsScheduler
                app.notificationHelper = notificationHelper
                app.deviceTimeHandler = deviceTimeHandler
                app.autoSubmission = autoSubmission
                app.coronaTestRepository = coronaTestRepository
                app.autoCheckOut = autoCheckOut
                app.traceLocationDbCleanupScheduler = traceLocationDbCleanupScheduler
                app.shareTestResultNotificationService = shareTestResultNotificationService
                app.exposureWindowRiskWorkScheduler = exposureWindowRiskWorkScheduler
                app.presenceTracingRiskWorkScheduler = presenceTracingRiskWorkScheduler
                app.pcrTestResultScheduler = pcrTestResultScheduler
                app.raTestResultScheduler = raTestResultScheduler
                app.pcrTestResultAvailableNotificationService = pcrTestResultAvailableNotificationService
                app.raTestResultAvailableNotificationService = raTestResultAvailableNotificationService
                app.testCertificateRetrievalScheduler = testCertificateRetrievalScheduler
                app.localStatisticsRetrievalScheduler = localStatisticsRetrievalScheduler
                app.securityProvider = securityProvider
                app.appScope = TestCoroutineScope()
                app.rollingLogHistory = object : Timber.Tree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        // NOOP
                    }
                }
                app.environmentSetup = environmentSetup
                app.imageLoaderFactory = imageLoaderFactory
                app.dccStateCheckScheduler = dscCheckScheduler
                app.recycleBinCleanUpScheduler = recycleBinCleanUpScheduler
                app.vaccinationStorage = vaccinationStorage
                app.cclConfigurationUpdaterScheduler = cclConfigurationUpdateScheduler
                app.familyTestResultRetrievalScheduler = familyTestResultRetrievalScheduler
                app.dccValidityStateChangeObserver = dccValidityStateChangeObserver
                app.dccRevocationUpdateScheduler = dccRevocationUpdateScheduler
            }
        }
    }

    private fun createInstance() = CoronaWarnApplication()

    @Test
    fun `all setups are called`() {
        createInstance().onCreate()

        verifySequence {

            watchdogService.launch()
            contactDiaryWorkScheduler.setup()

            deadmanNotificationScheduler.setup()

            exposureWindowRiskWorkScheduler.setup()
            presenceTracingRiskWorkScheduler.setup()

            pcrTestResultScheduler.setup()
            raTestResultScheduler.setup()
            familyTestResultRetrievalScheduler.setup()

            pcrTestResultAvailableNotificationService.setup()
            raTestResultAvailableNotificationService.setup()
            testCertificateRetrievalScheduler.setup()
            localStatisticsRetrievalScheduler.setup()

            deviceTimeHandler.launch()
            configChangeDetector.launch()
            ewRiskLevelChangeDetector.launch()
            combinedRiskLevelChangeDetector.launch()
            autoSubmission.setup()
            autoCheckOut.setupMonitor()
            traceLocationDbCleanupScheduler.scheduleDaily()
            shareTestResultNotificationService.setup()
            dscCheckScheduler.setup()
            recycleBinCleanUpScheduler.setup()
            cclConfigurationUpdateScheduler.setup()
            dccValidityStateChangeObserver.setup()
            dccRevocationUpdateScheduler.setup()
        }
    }
}
