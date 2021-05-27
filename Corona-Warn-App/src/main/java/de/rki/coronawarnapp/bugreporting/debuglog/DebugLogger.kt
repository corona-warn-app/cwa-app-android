package de.rki.coronawarnapp.bugreporting.debuglog

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLogStorageCheck
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLogTree
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogWriter
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.ApplicationComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import timber.log.Timber
import java.io.File

@SuppressLint("LogNotTimber", "StaticFieldLeak")
@Suppress("BlockingMethodInNonBlockingContext")
class DebugLogger(
    private val scope: CoroutineScope = DebugLoggerScope,
    private val context: Context,
    private val debugDir: File = File(context.cacheDir, "debuglog"),
    private val logWriter: LogWriter = LogWriter(File(debugDir, "debug.log"))
) : DebugLoggerBase() {

    private val triggerFile = File(debugDir, "debug.trigger")
    internal val runningLog: File
        get() = logWriter.logFile

    private val mutex = Mutex()
    private var logJob: Job? = null
    private var logTree: DebugLogTree? = null

    private var isDaggerReady = false

    val storageCheck = DebugLogStorageCheck(targetPath = debugDir, logWriter = logWriter)
    internal val isLogging = MutableStateFlow(false)

    val logState: Flow<LogState> = combine(
        storageCheck.isLowStorage,
        logWriter.logSize,
        isLogging
    ) { isLowStorage, logSize, isRunning ->
        LogState(
            isLogging = isRunning,
            isLowStorage = isLowStorage,
            logSize = logSize
        )
    }

    fun init() = try {
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

        Unit
    } catch (e: Exception) {
        // This is called via Application.onCreate() never crash here.
        Timber.tag(TAG).e(e, "DebugLogger init() failed.")
    }

    /**
     * To censor unique data, we need to actually know what to censor.
     * So we buffer log statements until Dagger is ready
     */
    fun setInjectionIsReady(component: ApplicationComponent) {
        Timber.tag(TAG).i("setInjectionIsReady()")
        component.inject(this)
        isDaggerReady = true
        Timber.tag(TAG).d("Censors loaded: %s", bugCensors)
    }

    suspend fun start(): Unit = mutex.withLock {
        Timber.tag(TAG).d("start()")

        if (logJob?.isActive == true) {
            Timber.tag(TAG).w("Ignoring start(), already running.")
            return@withLock
        }
        isLogging.value = true

        logJob?.cancel()
        logTree?.let { Timber.uproot(it) }

        if (!debugDir.exists()) {
            debugDir.mkdirs()
        }

        logTree = DebugLogTree().also { tree ->
            Timber.plant(tree)

            logWriter.setup()

            logJob = startNewLogJob(tree.logLines).apply {
                invokeOnCompletion { isLogging.value = false }
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

        logWriter.teardown()
    }

    private fun startNewLogJob(logLines: Flow<LogLine>) = scope.launch {
        try {
            logLines.collect { rawLine ->
                while (!isDaggerReady) {
                    yield()
                }

                if (storageCheck.isLowStorage()) return@collect

                launch {
                    // Censor data sources need a moment to know what to censor
                    delay(1000)

                    val formattedMessage = rawLine.format()
                    val censored: Collection<BugCensor.CensorContainer> = bugCensors.get()
                        .map {
                            async {
                                try {
                                    it.checkLog(formattedMessage)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in censor module $it", e)
                                    null
                                }
                            }
                        }
                        .awaitAll()
                        .filterNotNull()

                    val toWrite: String = when (censored.size) {
                        0 -> formattedMessage
                        1 -> censored.single().compile()?.censored ?: formattedMessage
                        else ->
                            try {
                                val combinedContainer = BugCensor.CensorContainer(
                                    original = formattedMessage,
                                    actions = censored.flatMap { it.actions }.toSet()
                                )

                                combinedContainer.compile()?.censored ?: formattedMessage
                            } catch (e: Exception) {
                                Log.e(TAG, "Censoring collision fail.", e)
                                "<censor-error>Global combination: $e</censor-error"
                            }
                    }
                    logWriter.write(rawLine.formatFinal(toWrite))
                }
            }
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Logging was canceled.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call appendLogLine(...)", e)
        }
    }

    companion object {
        internal const val TAG = "DebugLogger"
    }
}
