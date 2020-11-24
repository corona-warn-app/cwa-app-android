package de.rki.coronawarnapp.util.debug

import android.content.Context
import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber
import java.io.File

class FileLogger constructor(context: Context) {

    val logFile = File(context.cacheDir, "FileLoggerTree.log")

    private val blockerFile = File(context.filesDir, "FileLoggerTree.blocker")
    private var loggerTree: FileLoggerTree? = null

    val isLogging: Boolean
        get() = loggerTree != null

    init {
        if (!blockerFile.exists()) {
            start()
        }
    }

    fun start() {
        if (!CWADebug.isDeviceForTestersBuild) return

        if (loggerTree != null) return

        loggerTree = FileLoggerTree(logFile).also {
            Timber.plant(it)
            it.start()
            blockerFile.delete()
        }
    }

    fun stop() {
        if (!CWADebug.isDeviceForTestersBuild) return

        loggerTree?.let {
            it.stop()
            logFile.delete()
            blockerFile.createNewFile()
            loggerTree = null
        }
    }
}
