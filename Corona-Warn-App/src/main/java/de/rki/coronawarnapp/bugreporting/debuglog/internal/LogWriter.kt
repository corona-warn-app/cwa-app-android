package de.rki.coronawarnapp.bugreporting.debuglog.internal

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class LogWriter @Inject constructor(val logFile: File) {
    private var ioLimiter = 0
    val logSize = MutableStateFlow(logFile.length())

    private fun updateLogSize() {
        logSize.value = logFile.length()
    }

    fun setup() {
        if (!logFile.exists()) {
            logFile.parentFile?.mkdirs()
            if (logFile.createNewFile()) {
                Timber.i("Log file didn't exist and was created.")
            }
        }
        updateLogSize()
    }

    fun teardown() {
        if (logFile.exists() && logFile.delete()) {
            Timber.d("Log file was deleted.")
        }
        updateLogSize()
    }

    fun write(line: LogLine) {
        val formattedLine = line.format()
        logFile.appendText(formattedLine, Charsets.UTF_8)

        if (ioLimiter % 10 == 0) {
            updateLogSize()
            ioLimiter = 0
        }
        ioLimiter++
    }
}
