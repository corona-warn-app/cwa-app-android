package testhelpers.logging

import android.util.Log

import timber.log.Timber

class JUnitTree(private val minLogLevel: Int = Log.VERBOSE) : Timber.DebugTree() {

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.ERROR -> "E"
        Log.WARN -> "W"
        Log.INFO -> "I"
        Log.DEBUG -> "D"
        Log.VERBOSE -> "V"
        else -> priority.toString()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < minLogLevel) return
        println("${System.currentTimeMillis()} ${priorityToString(priority)}/$tag: $message")
    }
}
