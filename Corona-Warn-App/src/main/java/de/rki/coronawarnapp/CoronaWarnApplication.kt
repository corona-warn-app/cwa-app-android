package de.rki.coronawarnapp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import de.rki.coronawarnapp.notification.NotificationHelper

class CoronaWarnApplication : Application(), LifecycleObserver {

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

    override fun onCreate() {
        instance = this
        NotificationHelper.createNotificationChannel()
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
}
