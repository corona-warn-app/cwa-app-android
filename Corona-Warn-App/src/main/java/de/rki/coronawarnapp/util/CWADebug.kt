package de.rki.coronawarnapp.util

import android.app.Application
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.util.debug.FileLogger
import timber.log.Timber

object CWADebug {
    var fileLogger: FileLogger? = null

    fun init(application: Application) {
        if (isDebugBuildOrMode) System.setProperty("kotlinx.coroutines.debug", "on")

        if (isDeviceForTestersBuild) {
            Timber.plant(Timber.DebugTree())
        }
        if (isDeviceForTestersBuild) {
            fileLogger = FileLogger(application)
        }
    }

    val isDebugBuildOrMode: Boolean
        get() = BuildConfig.DEBUG || buildFlavor == BuildFlavor.DEVICE_FOR_TESTERS

    val buildFlavor: BuildFlavor
        get() = BuildFlavor.values().single { it.rawValue == BuildConfig.FLAVOR }

    val isDeviceForTestersBuild: Boolean = buildFlavor == BuildFlavor.DEVICE_FOR_TESTERS

    enum class BuildFlavor(val rawValue: String) {
        DEVICE("device"),
        DEVICE_FOR_TESTERS("deviceForTesters")
    }

    val isAUnitTest: Boolean by lazy {
        try {
            Class.forName("testhelpers.IsAUnitTest")
            true
        } catch (e: Exception) {
            false
        }
    }
}
