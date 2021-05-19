package de.rki.coronawarnapp.bugreporting.debuglog.internal

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject

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

    suspend fun write(line: LogLine) = mutex.withLock {
        val formattedLine = line.format()
        logFile.appendText(formattedLine, Charsets.UTF_8)

        if (ioLimiter % 10 == 0) {
            updateLogSize()
            ioLimiter = 0
        }
        ioLimiter++
    }
}
