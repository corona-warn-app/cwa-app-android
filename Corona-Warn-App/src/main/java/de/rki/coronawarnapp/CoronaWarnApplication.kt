package de.rki.coronawarnapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Configuration
import androidx.work.WorkManager
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import java.util.UUID

class CoronaWarnApplication : Application(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks {

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

        const val TEN_MINUTE_TIMEOUT_IN_MS = 10 * 60 * 1000L
    }

    private lateinit var errorReceiver: ErrorReportReceiver

    override fun onCreate() {
        super.onCreate()
        instance = this

        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManager.initialize(this, configuration)

        NotificationHelper.createNotificationChannel()
        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // notification to test the WakeUpService from Google when the app
        // was force stopped
        BackgroundWorkHelper.sendDebugNotification(
            "Application onCreate", "App was woken up"
        )
        // Only do this if the background jobs are enabled
        if (ConnectivityHelper.autoModeEnabled(applicationContext)) {
            ProcessLifecycleOwner.get().lifecycleScope.launch {
                // we want a wakelock as the OS does not handle this for us like in the background
                // job execution
                val wakeLock: PowerManager.WakeLock =
                    (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            TAG + "-WAKE-" + UUID.randomUUID().toString()
                        ).apply {
                            acquire(TEN_MINUTE_TIMEOUT_IN_MS)
                        }
                    }

                // we keep a wifi lock to wake up the wifi connection in case the device is dozing
                val wifiLock: WifiManager.WifiLock =
                    (getSystemService(Context.WIFI_SERVICE) as WifiManager).run {
                        createWifiLock(
                            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                            TAG + "-WIFI-" + UUID.randomUUID().toString()
                        ).apply {
                            acquire()
                        }
                    }

                try {
                    BackgroundWorkHelper.sendDebugNotification(
                        "Automatic mode is on", "Check if we have downloaded keys already today"
                    )
                    RetrieveDiagnosisKeysTransaction.startWithConstraints()
                } catch (e: Exception) {
                    BackgroundWorkHelper.sendDebugNotification(
                        "RetrieveDiagnosisKeysTransaction failed",
                        (e.localizedMessage
                            ?: "Unknown exception occurred in onCreate") + "\n\n" + (e.cause
                            ?: "Cause is unknown").toString()
                    )
                    // retry the key retrieval in case of an error with a scheduled work
                    BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
                }

                if (wifiLock.isHeld) wifiLock.release()
                if (wakeLock.isHeld) wakeLock.release()
            }

            // if the user is onboarded we will schedule period background jobs
            // in case the app was force stopped and woken up again by the Google WakeUpService
            if (LocalData.onboardingCompletedTimestamp() != null) BackgroundWorkScheduler.startWorkScheduler()
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
}
