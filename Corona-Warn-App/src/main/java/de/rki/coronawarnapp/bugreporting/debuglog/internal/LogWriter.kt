package de.rki.coronawarnapp.bugreporting.debuglog.internal

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
@SuppressLint("LogNotTimber")
class LogWriter @Inject constructor(val logFile: File) {
    private var ioLimiter = 0
    private val mutex = Mutex()
    val logSize = MutableStateFlow(logFile.length())

    private fun updateLogSize() {
        logSize.value = logFile.length()
    }

    suspend fun setup() = mutex.withLock {
        if (!logFile.exists()) {
            logFile.parentFile?.mkdirs()
            if (logFile.createNewFile()) {
                Timber.i("Log file didn't exist and was created.")
            }
        }
        updateLogSize()
    }

    suspend fun teardown() = mutex.withLock {
        if (logFile.exists() && logFile.delete()) {
            Timber.d("Log file was deleted.")
        }
        updateLogSize()
    }

    suspend fun write(formattedLine: String): Unit = mutex.withLock {
        val performWrite = {
            logFile.appendText(formattedLine + "\n", Charsets.UTF_8)
        }

        try {
            performWrite()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Log file didn't exist when we tried to write, retry.")

            try {
                logFile.parentFile?.mkdirs()
                logFile.createNewFile()
                logFile.writeText("Logfile was deleted.\n")

                performWrite()

                updateLogSize()
            } catch (e: Exception) {
                Log.e(TAG, "LogWrite retry failed, something is just wrong...", e)
                return@withLock
            }
        }

        if (ioLimiter % 10 == 0) {
            updateLogSize()
            ioLimiter = 0
        }
        ioLimiter++
    }

    companion object {
        private const val TAG = "LogWriter"
    }
}
