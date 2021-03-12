package de.rki.coronawarnapp.util.debug

import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber

class UncaughtExceptionLogger(
    private val wrappedHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    init {
        Timber.v("Wrapping exception handler: %s", wrappedHandler)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        Timber.tag(thread.name).e(error, "Uncaught exception!")
        try {
            if (CWADebug.isLogging) {
                // Make sure this crash is written before killing the app.
                Thread.sleep(1500)
            }
        } catch (e: Exception) {
            Timber.w("Couldn't delay exception for debug logger.")
        }
        wrappedHandler?.uncaughtException(thread, error)
    }

    companion object {
        fun wrapCurrentHandler() = UncaughtExceptionLogger(Thread.getDefaultUncaughtExceptionHandler()).also {
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
    }
}
