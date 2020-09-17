package de.rki.coronawarnapp.util.debug

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

@SuppressLint("LogNotTimber")
class FileLoggerTree(private val logFile: File) : Timber.DebugTree() {
    private var logWriter: OutputStreamWriter? = null

    @SuppressLint("SetWorldReadable")
    @Synchronized
    fun start() {
        if (logWriter != null) return

        logFile.parentFile.mkdirs()
        if (logFile.createNewFile()) {
            Log.i(TAG, "File logger writing to " + logFile.path)
        }
        if (logFile.setReadable(true, false)) {
            Log.i(TAG, "Debug run log read permission set")
        }

        try {
            logWriter = OutputStreamWriter(FileOutputStream(logFile, true))
            logWriter!!.write("=== BEGIN ===\n")
            logWriter!!.write("Logfile: $logFile\n")
            logWriter!!.flush()
            Log.i(TAG, "File logger started.")
        } catch (e: IOException) {
            e.printStackTrace()

            logFile.delete()
            if (logWriter != null) logWriter!!.close()
        }
    }

    @Synchronized
    fun stop() {
        logWriter?.let {
            logWriter = null
            try {
                it.write("=== END ===\n")
                it.close()
            } catch (ignore: IOException) {
            }
            Log.i(TAG, "File logger stopped.")
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logWriter?.let {
            try {
                it.write("${System.currentTimeMillis()}  ${priorityToString(priority)}/$tag: $message\n")
                it.flush()
            } catch (e: IOException) {
                Timber.tag(TAG).e(e)
                try {
                    it.close()
                } catch (ignore: Exception) {
                }
                logWriter = null
            }
        }
    }

    override fun toString(): String {
        return "FileLoggerTree(file=$logFile)"
    }

    companion object {
        private const val TAG = "FileLoggerTree"
        private fun priorityToString(priority: Int): String = when (priority) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            Log.DEBUG -> "D"
            Log.VERBOSE -> "V"
            else -> priority.toString()
        }
    }
}
