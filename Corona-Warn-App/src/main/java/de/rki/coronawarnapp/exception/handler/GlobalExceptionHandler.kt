package de.rki.coronawarnapp.exception.handler

import android.content.Context
import android.content.Intent
import android.util.Log
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.ui.LauncherActivity
import java.lang.reflect.InvocationTargetException

class GlobalExceptionHandler(private val application: CoronaWarnApplication) :
    Thread.UncaughtExceptionHandler {

    companion object {
        val TAG: String? = GlobalExceptionHandler::class.simpleName
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.i(TAG, "cause caught: " + throwable)
            val cause = throwable.cause
            val exceptionMessage: String

            // Throwables from main thread are wrapped in an InvocationTargetException,
            // unwrap the InvocationTargetException to get the original cause
            if (cause is InvocationTargetException) {
                exceptionMessage = cause.targetException.toString()
                Log.i(TAG, "InvocationTargetException caught: " + cause.targetException)
            }
            // for errors thrown by coroutines, these are not wrapped in InvocationTargetException
            else {
                Log.i(TAG, "InvocationTargetException caught: " + throwable)
                exceptionMessage = throwable.toString()
            }
            triggerRestart(CoronaWarnApplication.getAppContext(), exceptionMessage)
        } catch (e: Exception) {
            Log.e(TAG, "GlobalExceptionHandler failing" + e)
        }
    }

    /**
     * Restarts the app by sending an Intent to start LauncherActivitiy and
     * terminating the JVM
     *
     * @param context application context
     * @param exceptionMessage exception that caused the crash
     */
    private fun triggerRestart(context: Context, exceptionMessage: String) {
        val intent = Intent(context, LauncherActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK
        )
        intent.putExtra("crashInfo", exceptionMessage)
        intent.putExtra("appCrashed", true)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
