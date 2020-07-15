package de.rki.coronawarnapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.notification.NotificationHelper
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import javax.inject.Inject

@HiltAndroidApp
class CoronaWarnApplication : Application(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks,
    Configuration.Provider {

    companion object {
        val TAG: String? = CoronaWarnApplication::class.simpleName
        private lateinit var instance: CoronaWarnApplication

        /* describes if the app is in foreground
         * Initialized to false, because app could also be started by a background job.
         * For the cases where the app is started via the launcher icon, the onAppForegrounded
         * event will be called, setting it to true
         */
        var isAppInForeground = false

        fun getAppContext(): Context =
            instance.applicationContext
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private lateinit var errorReceiver: ErrorReportReceiver

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.createNotificationChannel()
        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /**
     * Callback when the app is open but backgrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
        Timber.v("App backgrounded")
    }

    /**
     * Callback when the app is foregrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
        Timber.v("App foregrounded")
    }

    override fun onActivityPaused(activity: Activity) {
        // unregisters error receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorReceiver)
    }

    override fun onActivityStarted(activity: Activity) {
        // does not override function. Empty on intention
    }

    override fun onActivityDestroyed(activity: Activity) {
        // does not override function. Empty on intention
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // does not override function. Empty on intention
    }

    override fun onActivityStopped(activity: Activity) {
        // does not override function. Empty on intention
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // set screen orientation to portrait
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    override fun onActivityResumed(activity: Activity) {
        errorReceiver =
            ErrorReportReceiver(activity)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(errorReceiver, IntentFilter(ERROR_REPORT_LOCAL_BROADCAST_CHANNEL))
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
