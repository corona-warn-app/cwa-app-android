package de.rki.coronawarnapp.util.debug

import android.content.Context
import timber.log.Timber
import java.io.File

class FileLogger constructor(private val context: Context) {

    val logFile = File(context.cacheDir, "FileLoggerTree.log")
    val triggerFile = File(context.filesDir, "FileLoggerTree.trigger")
    private var loggerTree: FileLoggerTree? = null

    val isLogging: Boolean
        get() = loggerTree != null

    init {
        if (triggerFile.exists()) {
            start()
        }
    }

    fun start() {
        if (loggerTree != null) return

        loggerTree = FileLoggerTree(logFile).also {
            Timber.plant(it)
            it.start()
            triggerFile.createNewFile()
        }
    }

    fun stop() {
        loggerTree?.let {
            it.stop()
            logFile.delete()
            triggerFile.delete()
            loggerTree = null
        }
    }
}
