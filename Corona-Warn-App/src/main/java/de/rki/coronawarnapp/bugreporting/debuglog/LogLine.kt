package de.rki.coronawarnapp.bugreporting.debuglog

import android.util.Log
import org.joda.time.Instant
import java.io.PrintWriter
import java.io.StringWriter

data class LogLine(
    val timestamp: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val throwable: Throwable?
) {

    fun format(): String {
        val time = Instant.ofEpochMilli(timestamp)

        val baseLine = "$time ${priorityLabel(priority)}/$tag: $message\n"

        return if (throwable != null) {
            baseLine + "\n" + getStackTraceString(throwable)
        } else {
            baseLine
        }
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
