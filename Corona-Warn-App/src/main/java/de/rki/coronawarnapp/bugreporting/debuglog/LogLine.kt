package de.rki.coronawarnapp.bugreporting.debuglog

import android.util.Log
import org.joda.time.Instant

data class LogLine(
    val timestamp: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val throwable: Throwable?
) {

    fun format(): String {
        val time = Instant.ofEpochMilli(timestamp)
        return "$time  ${priorityLabel(priority)}/$tag: $message\n"
    }

    private fun priorityLabel(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.INFO -> "I"
        Log.DEBUG -> "D"
        Log.VERBOSE -> "V"
        else -> priority.toString()
    }
}
