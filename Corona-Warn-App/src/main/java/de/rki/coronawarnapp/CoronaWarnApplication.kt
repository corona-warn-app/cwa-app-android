package de.rki.coronawarnapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import coil.Coil
import coil.ImageLoaderFactory
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.appconfig.ConfigChangeDetector
import de.rki.coronawarnapp.appconfig.devicetime.DeviceTimeHandler
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdateScheduler
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler
import de.rki.coronawarnapp.coronatest.type.rapidantigen.notification.RATTestResultAvailableNotificationService
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateCheckScheduler
import de.rki.coronawarnapp.covidcertificate.revocation.update.RevocationUpdateScheduler
import de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalScheduler
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
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
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.WatchdogService
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.hasAPILevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class CoronaWarnApplication : Application(), HasAndroidInjector {

    @Inject lateinit var component: ApplicationComponent
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @Inject lateinit var watchdogService: WatchdogService
    @Inject lateinit var taskController: TaskController
    @Inject lateinit var foregroundState: ForegroundState
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var configChangeDetector: ConfigChangeDetector
    @Inject lateinit var ewRiskLevelChangeDetector: EwRiskLevelChangeDetector
    @Inject lateinit var combinedRiskLevelChangeDetector: CombinedRiskLevelChangeDetector
    @Inject lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @Inject lateinit var contactDiaryWorkScheduler: ContactDiaryWorkScheduler
    @Inject lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler
    @Inject lateinit var notificationHelper: GeneralNotifications
    @Inject lateinit var deviceTimeHandler: DeviceTimeHandler
    @Inject lateinit var autoSubmission: AutoSubmission
    @Inject lateinit var coronaTestRepository: CoronaTestRepository
    @Inject lateinit var autoCheckOut: AutoCheckOut
    @Inject lateinit var traceLocationDbCleanupScheduler: TraceLocationDbCleanUpScheduler
    @Inject lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @Inject lateinit var exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler
    @Inject lateinit var presenceTracingRiskWorkScheduler: PresenceTracingRiskWorkScheduler
    @Inject lateinit var pcrTestResultScheduler: PCRResultScheduler
    @Inject lateinit var raTestResultScheduler: RAResultScheduler
    @Inject lateinit var pcrTestResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @Inject lateinit var raTestResultAvailableNotificationService: RATTestResultAvailableNotificationService
    @Inject lateinit var testCertificateRetrievalScheduler: TestCertificateRetrievalScheduler
    @Inject lateinit var environmentSetup: EnvironmentSetup
    @Inject lateinit var localStatisticsRetrievalScheduler: LocalStatisticsRetrievalScheduler
    @Inject lateinit var imageLoaderFactory: ImageLoaderFactory
    @Inject lateinit var dccStateCheckScheduler: DccStateCheckScheduler
    @Inject lateinit var securityProvider: SecurityProvider
    @Inject lateinit var recycleBinCleanUpScheduler: RecycleBinCleanUpScheduler
    @Inject lateinit var vaccinationStorage: VaccinationStorage
    @Inject lateinit var cclConfigurationUpdaterScheduler: CclConfigurationUpdateScheduler
    @Inject lateinit var revocationUpdateScheduler: RevocationUpdateScheduler

    @AppScope
    @Inject lateinit var appScope: CoroutineScope

    @LogHistoryTree @Inject lateinit var rollingLogHistory: Timber.Tree

    override fun onCreate() {
        instance = this
        super.onCreate()
        CWADebug.init(this)

        AppInjector.init(this).let { compPreview ->
            if (BuildVersionWrap.hasAPILevel(23)) {
                Timber.v("Calling EncryptedPreferencesMigration.doMigration()")
                compPreview.encryptedMigration.get().doMigration()
            }

            CWADebug.initAfterInjection(compPreview)

            Timber.v("Completing application injection")
            compPreview.inject(this)
        }

        Timber.plant(rollingLogHistory)

        Timber.v("onCreate(): WorkManager setup done: $workManager")

        securityProvider.setup()
        // See de.rki.coronawarnapp.util.coil.CoilModule::class
        Coil.setImageLoader(imageLoaderFactory)

        registerActivityLifecycleCallbacks(activityLifecycleCallback)

        watchdogService.launch()

        foregroundState.isInForeground
            .onEach { isAppInForeground = it }
            .launchIn(appScope)

        environmentSetup.sanityCheck()

        Timber.v("Setting up contact diary work scheduler")
        contactDiaryWorkScheduler.setup()

        Timber.v("Setting up deadman notification scheduler")
        deadmanNotificationScheduler.setup()

        Timber.v("Setting up risk work schedulers.")
        exposureWindowRiskWorkScheduler.setup()
        presenceTracingRiskWorkScheduler.setup()

        Timber.v("Setting up test result work schedulers.")
        pcrTestResultScheduler.setup()
        raTestResultScheduler.setup()

        Timber.v("Setting up test result available notification services.")
        pcrTestResultAvailableNotificationService.setup()
        raTestResultAvailableNotificationService.setup()
        testCertificateRetrievalScheduler.setup()

        Timber.v("Setting up local statistics update scheduler")
        localStatisticsRetrievalScheduler.setup()

        deviceTimeHandler.launch()
        configChangeDetector.launch()
        ewRiskLevelChangeDetector.launch()
        combinedRiskLevelChangeDetector.launch()
        autoSubmission.setup()
        autoCheckOut.setupMonitor()
        traceLocationDbCleanupScheduler.scheduleDaily()
        shareTestResultNotificationService.setup()
        dccStateCheckScheduler.setup()
        recycleBinCleanUpScheduler.setup()
        cclConfigurationUpdaterScheduler.setup()
        revocationUpdateScheduler.setup()
    }

    private val activityLifecycleCallback = object : ActivityLifecycleCallbacks {
        private val localBM by lazy {
            LocalBroadcastManager.getInstance(this@CoronaWarnApplication)
        }
        private var errorReceiver: ErrorReportReceiver? = null

        override fun onActivityPaused(activity: Activity) {
            errorReceiver?.let {
                localBM.unregisterReceiver(it)
                errorReceiver = null
            }
            disableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            enableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            // NOOP
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // NOOP
        }

        override fun onActivityStopped(activity: Activity) {
            disableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // NOOP
        }

        override fun onActivityResumed(activity: Activity) {
            errorReceiver?.let {
                localBM.unregisterReceiver(it)
                errorReceiver = null
            }

            errorReceiver = ErrorReportReceiver(activity).also {
                localBM.registerReceiver(it, IntentFilter(ERROR_REPORT_LOCAL_BROADCAST_CHANNEL))
            }
            enableAppLauncherPreviewAndScreenshots(activity)
        }
    }

    private fun enableAppLauncherPreviewAndScreenshots(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun disableAppLauncherPreviewAndScreenshots(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    companion object {
        private lateinit var instance: CoronaWarnApplication

        /* describes if the app is in foreground
         * Initialized to false, because app could also be started by a background job.
         * For the cases where the app is started via the launcher icon, the onAppForegrounded
         * event will be called, setting it to true
         */
        var isAppInForeground = false

        fun getAppContext(): Context = instance.applicationContext
    }
}
