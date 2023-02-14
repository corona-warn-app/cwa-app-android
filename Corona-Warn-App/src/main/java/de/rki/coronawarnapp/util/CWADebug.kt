package de.rki.coronawarnapp.util

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.bugreporting.debuglog.DebugEntryPoint
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.debug.UncaughtExceptionLogger
import timber.log.Timber

object CWADebug {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var debugLoggerFactory: (Application) -> DebugLogger = { DebugLogger(context = it) }

    @SuppressLint("StaticFieldLeak")
    lateinit var debugLogger: DebugLogger

    fun init(application: Application) {
        if (isDebugBuildOrMode) System.setProperty("kotlinx.coroutines.debug", "on")

        if (isDeviceForTestersBuild) {
            Timber.plant(Timber.DebugTree())
        }

        setupExceptionHandler()

        debugLogger = debugLoggerFactory(application).also {
            it.init()
        }

        logDeviceInfos()
    }

    fun initAfterInjection(point: DebugEntryPoint) {
        Timber.v("initAfterInjection")
        debugLogger.setInjectionIsReady(point)
    }

    val isLogging: Boolean
        get() {
            if (!this::debugLogger.isInitialized) return false
            return debugLogger.isLogging.value
        }

    val isDebugBuildOrMode: Boolean
        get() = BuildConfigWrap.DEBUG || buildFlavor == BuildFlavor.DEVICE_FOR_TESTERS

    val buildFlavor: BuildFlavor
        get() = BuildFlavor.values().single { it.rawValue == BuildConfigWrap.FLAVOR }

    val isDeviceForTestersBuild: Boolean
        get() = buildFlavor == BuildFlavor.DEVICE_FOR_TESTERS

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

    fun logDeviceInfos() {
        Timber.i("CWA version: %s (%s)", BuildConfigWrap.VERSION_CODE, BuildConfigWrap.GIT_COMMIT_SHORT_HASH)
        Timber.i("CWA flavor: %s (%s)", BuildConfigWrap.FLAVOR, BuildConfigWrap.BUILD_TYPE)
        Timber.i("Build.FINGERPRINT: %s", Build.FINGERPRINT)
    }

    /**
     * Allow internal logging via `DebugLogger` to log stacktraces for uncaught exceptions.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setupExceptionHandler() {
        UncaughtExceptionLogger.wrapCurrentHandler()
    }
}
