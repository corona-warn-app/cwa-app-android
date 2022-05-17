package de.rki.coronawarnapp.bugreporting.debuglog

import android.util.Log
import java.time.Instant
import java.io.PrintWriter
import java.io.StringWriter

data class LogLine(
    val timestamp: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val throwable: Throwable?
) {

    fun format(): String = if (throwable != null) {
        message + "\n" + getStackTraceString(throwable)
    } else {
        message
    }

    fun formatFinal(formattedLogLine: String): String {
        val time = Instant.ofEpochMilli(timestamp)
        return "$time ${priorityLabel(priority)}/$tag: $formattedLogLine"
    }

    companion object {
        // Based on Timber.Tree.getStackTraceString()
        internal fun getStackTraceString(t: Throwable): String {
            val sw = StringWriter(256)
            val pw = PrintWriter(sw, false)
            t.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }

        internal fun priorityLabel(priority: Int): String = when (priority) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            Log.DEBUG -> "D"
            Log.VERBOSE -> "V"
            else -> priority.toString()
        }
    }
}
