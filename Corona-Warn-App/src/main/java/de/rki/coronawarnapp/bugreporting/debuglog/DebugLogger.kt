package de.rki.coronawarnapp.bugreporting.debuglog

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.ApplicationComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import timber.log.Timber
import java.io.File

@SuppressLint("LogNotTimber")
@Suppress("BlockingMethodInNonBlockingContext")
object DebugLogger : DebugLoggerBase() {

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
    private var isDaggerReady = false

    fun init(application: Application) {
        context = application

        try {
            val startLogger = when {
                triggerFile.exists() -> {
                    Timber.tag(TAG).i("Trigger file exists, starting debug log.")
                    true
                }
                CWADebug.isDeviceForTestersBuild -> {
                    Timber.tag(TAG).i("Trigger file does not exist, but it's a tester build, starting debug log.")
                    true
                }
                else -> false
            }

            if (startLogger) {
                runBlocking { start() }
            }
        } catch (e: Exception) {
            // This is called from Application.onCreate() never crash here.
            Timber.tag(TAG).e(e, "DebugLogger init(%s) failed.", application)
        }
    }

    /**
     * To censor unique data, we need to actually know what to censor.
     * So we buffer log statements until Dagger is ready
     */
    fun setInjectionIsReady(component: ApplicationComponent) {
        Timber.tag(TAG).i("setInjectionIsReady()")
        component.inject(this)
        isDaggerReady = true
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

            if (!runningLog.exists()) {
                runningLog.parentFile?.mkdirs()
                if (runningLog.createNewFile()) {
                    Timber.tag(TAG).i("Log file didn't exist and was created.")
                }
            }

            logJob = scope.launch {
                try {
                    logLines.collect { rawLine ->
                        while (!isDaggerReady) {
                            yield()
                        }
                        launch {
                            // Censor data sources need a moment to know what to censor
                            delay(1000)
                            val censoredLine = bugCensors.get().fold(rawLine) { prev, censor ->
                                censor.checkLog(prev) ?: prev
                            }
                            appendLogLine(censoredLine)
                        }
                    }
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
        Timber.tag(TAG).i("stop()")

        if (triggerFile.exists() && triggerFile.delete()) {
            Timber.tag(TAG).d("Trigger file deleted.")
        }

        logTree?.let {
            Timber.tag(TAG).d("LogTree uprooted.")
            Timber.uproot(it)
        }
        logTree = null

        logJob?.let {
            Timber.tag(TAG).d("LogJob canceled.")
            it.cancel()
            it.join()
        }
        logJob = null

        if (runningLog.exists() && runningLog.delete()) {
            Timber.tag(TAG).d("Log file was deleted.")
        }

        clearSharedFiles()
    }

    private fun appendLogLine(line: LogLine) {
        val formattedLine = line.format()
        runningLog.appendText(formattedLine, Charsets.UTF_8)
    }

    fun getLogSize(): Long = runningLog.length()

    fun getShareSize(): Long = sharedDirectory.listFiles()
        ?.fold(0L) { prev, file -> prev + file.length() }
        ?: 0L

    fun clearSharedFiles() {
        if (!sharedDirectory.exists()) return

        sharedDirectory.listFiles()?.forEach {
            if (it.delete()) {
                Timber.tag(TAG).d("Deleted shared file: %s", it)
            } else {
                Timber.tag(TAG).w("Failed to delete shared file: %s", it)
            }
        }
    }

    private const val TAG = "DebugLogger"
}
