package de.rki.coronawarnapp.util.debug

import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import timber.log.Timber

class UncaughtExceptionLogger(
    private val wrappedHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    init {
        Timber.v("Wrapping exception handler: %s", wrappedHandler)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        Timber.tag(thread.name).e(error, "Uncaught exception!")
        if (DebugLogger.isLogging) {
            // Make sure this crash is written before killing the app.
            Thread.sleep(1500)
        }
        wrappedHandler?.uncaughtException(thread, error)
    }

    companion object {
        fun wrapCurrentHandler() = UncaughtExceptionLogger(Thread.getDefaultUncaughtExceptionHandler()).also {
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
    }
}
