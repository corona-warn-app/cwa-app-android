package de.rki.coronawarnapp.logging

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogElementHelper {

    private val sdf: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)
    }

    fun logElementToString(logElement: LogElement): String {
        var logLine =
            "${sdf.format(Date())}  ${priorityToString(logElement.priority)}/${logElement.tag}: ${logElement.message}"

        logElement.t?.let {
            logLine += System.lineSeparator()
            logLine += Log.getStackTraceString(it)
        }

        return logLine
    }

    fun priorityToString(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.INFO -> "I"
        Log.DEBUG -> "D"
        Log.VERBOSE -> "V"
        else -> priority.toString()
    }
}
