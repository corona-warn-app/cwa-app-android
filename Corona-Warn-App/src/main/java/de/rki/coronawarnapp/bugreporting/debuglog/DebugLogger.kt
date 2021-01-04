package de.rki.coronawarnapp.bugreporting.debuglog

import android.content.Context
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Suppress("BlockingMethodInNonBlockingContext")
@Singleton
class DebugLogger @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val logTreeProvider: Provider<DebugLogTree>
) {
    private val debugDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val debugDir by lazy {
        File(context.cacheDir, "debuglog").also {
            if (!it.exists()) it.mkdir()
        }
    }

    private val triggerFile by lazy {
        File(debugDir, "debug.trigger")
    }

    internal val runningLog by lazy {
        File(debugDir, "debug.log")
    }
    val sharedDirectory by lazy {
        File(debugDir, "shared")
    }

    private val mutex = Mutex()

    private var logJob: Job? = null
    private var logTree: DebugLogTree? = null

    init {
        if (triggerFile.exists()) {
            Timber.tag(TAG).i("Trigger file exists, starting debug log.")
            appScope.launch(debugDispatcher) {
                start()
            }
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

        logTreeProvider.get().apply {
            if (!runningLog.exists() && runningLog.createNewFile()) {
                Timber.tag(TAG).i("Log file didn't exist and was created.")
            }
            logJob = logLines
                .onEach { appendLogLine(it) }
                .catch {
                    Timber.tag(TAG).e(it, "Logging error")
                }
                .launchIn(scope = appScope + debugDispatcher)

            logTree = this
            Timber.plant(this)
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

    companion object {
        private const val TAG = "DebugLogger"
    }
}
