package de.rki.coronawarnapp

import android.app.Application
import android.content.Context

class CoronaWarnApplication : Application() {

    companion object {
        private lateinit var instance: CoronaWarnApplication
        fun getAppContext(): Context =
            instance.applicationContext
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}
