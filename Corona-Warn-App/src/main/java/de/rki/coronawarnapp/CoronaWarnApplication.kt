package de.rki.coronawarnapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.rki.coronawarnapp.exception.ErrorReportReceiver
import de.rki.coronawarnapp.exception.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.exception.handler.GlobalExceptionHandler
import de.rki.coronawarnapp.notification.NotificationHelper

class CoronaWarnApplication : Application(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    companion object {
        val TAG: String? = CoronaWarnApplication::class.simpleName
        private lateinit var instance: CoronaWarnApplication

        private lateinit var currentActivity: Activity

        /* describes if the app is in foreground
         * Initialized to false, because app could also be started by a background job.
         * For the cases where the app is started via the launcher icon, the onAppForegrounded
         * event will be called, setting it to true
         */
        var isAppInForeground = false

        fun getAppContext(): Context =
            instance.applicationContext

        fun getCurrentActivity(): Activity {
            return currentActivity
        }
    }

    private val errorReceiver = ErrorReportReceiver()

    override fun onCreate() {
        super.onCreate()
        GlobalExceptionHandler(this)
        instance = this
        NotificationHelper.createNotificationChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)
    }

    /**
     * Callback when the app is open but backgrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
        Log.v(TAG, "App backgrounded")
    }

    /**
     * Callback when the app is foregrounded
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
        Log.v(TAG, "App foregrounded")
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
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // prevents screenshot of the app for all activities
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        // set screen orientation to portrait
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    override fun onActivityResumed(activity: Activity) {
        // registers error receiver
        currentActivity = activity
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(errorReceiver, IntentFilter(ERROR_REPORT_LOCAL_BROADCAST_CHANNEL))
    }
}
