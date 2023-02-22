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
import dagger.Lazy
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import de.rki.coronawarnapp.bugreporting.debuglog.DebugEntryPoint
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.initializer.AppStarter
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesMigration
import de.rki.coronawarnapp.util.hasAPILevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
open class CoronaWarnApplication : Application() {

    @Inject lateinit var appStarter: AppStarter
    @Inject lateinit var appStarter: Lazy<AppStarter>
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var imageLoaderFactory: ImageLoaderFactory
    @Inject lateinit var foregroundState: ForegroundState
    @AppScope @Inject lateinit var appScope: CoroutineScope
    @LogHistoryTree @Inject lateinit var rollingLogHistory: Timber.Tree
    @Inject lateinit var encryptedPreferencesMigration: Lazy<EncryptedPreferencesMigration>

    override fun onCreate() {
        instance = this
        super.onCreate()
        CWADebug.init(this)

        if (BuildVersionWrap.hasAPILevel(23)) {
            Timber.v("Calling EncryptedPreferencesMigration.doMigration()")
            encryptedPreferencesMigration.get().doMigration()
        }
        CWADebug.initAfterInjection(
            EntryPoints.get(applicationContext, DebugEntryPoint::class.java)
        )

        appStarter.get().start()

        Timber.plant(rollingLogHistory)

        Timber.v("onCreate(): WorkManager setup done: $workManager")

        // See de.rki.coronawarnapp.util.coil.CoilModule::class
        Coil.setImageLoader(imageLoaderFactory)

        registerActivityLifecycleCallbacks(activityLifecycleCallback)

        foregroundState.isInForeground
            .onEach { isAppInForeground = it }
            .launchIn(appScope)
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

        override fun onActivityStarted(activity: Activity) = enableAppLauncherPreviewAndScreenshots(activity)
        override fun onActivityStopped(activity: Activity) = disableAppLauncherPreviewAndScreenshots(activity)

        override fun onActivityDestroyed(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
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
