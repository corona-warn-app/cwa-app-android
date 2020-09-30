package de.rki.coronawarnapp.util

import android.app.Application
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.util.debug.FileLogger
import timber.log.Timber

object CWADebug {
    var fileLogger: FileLogger? = null

    fun init(application: Application) {
        if (isDebugBuildOrMode) System.setProperty("kotlinx.coroutines.debug", "on")

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        if ((BuildConfig.FLAVOR == "deviceForTesters" || BuildConfig.DEBUG)) {
            fileLogger = FileLogger(application)
        }
    }

    val isDebugBuildOrMode: Boolean
        get() = BuildConfig.DEBUG || BuildConfig.BUILD_VARIANT == "deviceForTesters"
}
