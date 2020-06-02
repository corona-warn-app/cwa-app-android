package de.rki.coronawarnapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import de.rki.coronawarnapp.notification.NotificationHelper

class CoronaWarnApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        private lateinit var instance: CoronaWarnApplication
        fun getAppContext(): Context =
            instance.applicationContext
    }

    override fun onCreate() {
        instance = this
        NotificationHelper.createNotificationChannel()
        super.onCreate()

        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        p0.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityDestroyed(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivityResumed(p0: Activity) {}
}
