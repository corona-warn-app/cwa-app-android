package de.rki.coronawarnapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.appconfig.ConfigChangeDetector
import de.rki.coronawarnapp.appconfig.devicetime.DeviceTimeHandler
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpScheduler
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.risk.RiskLevelChangeDetector
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.WatchdogService
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
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
    @Inject lateinit var riskLevelChangeDetector: RiskLevelChangeDetector
    @Inject lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @Inject lateinit var contactDiaryWorkScheduler: ContactDiaryWorkScheduler
    @Inject lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler
    @Inject lateinit var notificationHelper: GeneralNotifications
    @Inject lateinit var deviceTimeHandler: DeviceTimeHandler
    @Inject lateinit var autoSubmission: AutoSubmission
    @Inject lateinit var coronaTestRepository: CoronaTestRepository
    @Inject lateinit var onboardingSettings: OnboardingSettings
    @Inject lateinit var autoCheckOut: AutoCheckOut
    @Inject lateinit var traceLocationDbCleanupScheduler: TraceLocationDbCleanUpScheduler
    @Inject lateinit var backgroundWorkScheduler: BackgroundWorkScheduler

    @LogHistoryTree @Inject lateinit var rollingLogHistory: Timber.Tree

    override fun onCreate() {
        instance = this
        super.onCreate()
        CWADebug.init(this)

        AppInjector.init(this).let { compPreview ->
            Timber.v("Calling EncryptedPreferencesMigration.doMigration()")
            compPreview.encryptedMigration.doMigration()

            CWADebug.initAfterInjection(compPreview)

            Timber.v("Completing application injection")
            compPreview.inject(this)
        }

        Timber.plant(rollingLogHistory)

        Timber.v("onCreate(): WorkManager setup done: $workManager")

        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        registerActivityLifecycleCallbacks(activityLifecycleCallback)

        watchdogService.launch()

        foregroundState.isInForeground
            .onEach { isAppInForeground = it }
            .launchIn(GlobalScope)

        if (onboardingSettings.isOnboarded) {
            // TODO this is on the main thread, not very nice...
            runBlocking {
                val isAllowedToSubmitKeys = coronaTestRepository.coronaTests.first().any { it.isSubmissionAllowed }
                if (!isAllowedToSubmitKeys) {
                    deadmanNotificationScheduler.schedulePeriodic()
                }
            }

            contactDiaryWorkScheduler.schedulePeriodic()
        }

        deviceTimeHandler.launch()
        configChangeDetector.launch()
        riskLevelChangeDetector.launch()
        autoSubmission.setup()
        autoCheckOut.setupMonitor()
        traceLocationDbCleanupScheduler.scheduleDaily()
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
