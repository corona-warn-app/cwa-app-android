package de.rki.coronawarnapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.ForegroundState
import de.rki.coronawarnapp.util.WatchdogService
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    override fun onCreate() {
        instance = this
        super.onCreate()
        CWADebug.init(this)

        Timber.v("onCreate(): Initializing Dagger")
        AppInjector.init(this)

        Timber.v("onCreate(): Initializing WorkManager")
        Configuration.Builder()
            .apply { setMinimumLoggingLevel(android.util.Log.DEBUG) }.build()
            .let { WorkManager.initialize(this, it) }

        NotificationHelper.createNotificationChannel()

        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        registerActivityLifecycleCallbacks(activityLifecycleCallback)

        // notification to test the WakeUpService from Google when the app was force stopped
        BackgroundWorkHelper.sendDebugNotification(
            "Application onCreate", "App was woken up"
        )
        watchdogService.launch()

        foregroundState.isInForeground
            .onEach { isAppInForeground = it }
            .launchIn(GlobalScope)
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
        }

        override fun onActivityStarted(activity: Activity) {
            // NOOP
        }

        override fun onActivityDestroyed(activity: Activity) {
            // NOOP
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // NOOP
        }

        override fun onActivityStopped(activity: Activity) {
            // NOOP
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
        }
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
