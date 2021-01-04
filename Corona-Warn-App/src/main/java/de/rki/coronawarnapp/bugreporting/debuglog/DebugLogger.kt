package de.rki.coronawarnapp.bugreporting.debuglog

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File

@SuppressLint("LogNotTimber")
@Suppress("BlockingMethodInNonBlockingContext")
object DebugLogger {
    private val scope = DebugLoggerScope
    private lateinit var context: Context

    private val debugDir by lazy {
        File(context.cacheDir, "debuglog").also {
            if (!it.exists()) it.mkdir()
        }
    }

    private val triggerFile by lazy { File(debugDir, "debug.trigger") }

    internal val runningLog by lazy { File(debugDir, "debug.log") }
    val sharedDirectory by lazy { File(debugDir, "shared") }

    private val mutex = Mutex()

    private var logJob: Job? = null
    private var logTree: DebugLogTree? = null

    fun init(application: Application) {
        context = application

        if (triggerFile.exists()) {
            Timber.tag(TAG).i("Trigger file exists, starting debug log.")
            runBlocking { start() }
        }
    }

    val isLogging: Boolean
        get() = logJob?.isActive == true

    suspend fun start(): Unit = mutex.withLock {
        Timber.tag(TAG).d("start()")

        if (isLogging) {
            Timber.tag(TAG).w("Ignoring start(), already running.")
            return@withLock
        }

        logJob?.cancel()
        logTree?.let { Timber.uproot(it) }

        DebugLogTree().apply {
            Timber.plant(this)
            logTree = this

            if (!runningLog.exists() && runningLog.createNewFile()) {
                Timber.tag(TAG).i("Log file didn't exist and was created.")
            }

            logJob = scope.launch {
                try {
                    logLines.collect { appendLogLine(it) }
                } catch (e: CancellationException) {
                    Timber.tag(TAG).i("Logging was canceled.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to call appendLogLine(...)", e)
                }
            }
        }

        if (!triggerFile.exists()) {
            Timber.tag(TAG).i("Trigger file created.")
            triggerFile.createNewFile()
        }
    }

    suspend fun stop() = mutex.withLock {
        Timber.tag(TAG).d("stop()")

        if (!triggerFile.exists()) {
            Timber.tag(TAG).w("We are not logging, ignoring stop().")
            return@withLock
        }

        if (triggerFile.delete()) {
            Timber.tag(TAG).i("Trigger file deleted.")
        }

        logTree?.let { Timber.uproot(it) }
        logTree = null

        logJob?.cancel()
        logJob = null

        if (runningLog.exists() && runningLog.delete()) {
            Timber.tag(TAG).i("Log file was deleted.")
        }

        if (sharedDirectory.exists()) {
            val shared = sharedDirectory.list()
            if (sharedDirectory.deleteRecursively()) {
                Timber.tag(TAG).i("Deleted shared files: %s", shared?.joinToString(", "))
            }
        }
    }

    private fun appendLogLine(line: LogLine) {
        val formattedLine = line.format(context)
        runningLog.appendText(formattedLine, Charsets.UTF_8)
    }

    fun getLogSize(): Long = runningLog.length()

    fun getShareSize(): Long = sharedDirectory.listFiles()
        ?.fold(0L) { prev, file -> prev + file.length() }
        ?: 0L

    private const val TAG = "DebugLogger"
}
