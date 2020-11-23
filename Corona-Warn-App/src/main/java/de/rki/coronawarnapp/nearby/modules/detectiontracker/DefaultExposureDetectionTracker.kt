package de.rki.coronawarnapp.nearby.modules.detectiontracker

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection.Result
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class DefaultExposureDetectionTracker @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: ExposureDetectionTrackerStorage,
    private val timeStamper: TimeStamper,
    private val appConfigProvider: AppConfigProvider
) : ExposureDetectionTracker {

    init {
        Timber.v("init()")
    }

    private val detectionStates: HotDataFlow<Map<String, TrackedExposureDetection>> by lazy {
        val setupAutoSave: (HotDataFlow<Map<String, TrackedExposureDetection>>) -> Unit = { hd ->
            hd.data
                .onStart { Timber.v("Observing detection changes.") }
                .onEach { storage.save(it) }
                .launchIn(scope = scope + dispatcherProvider.Default)
        }

        val setupTimeoutEnforcer: (HotDataFlow<Map<String, TrackedExposureDetection>>) -> Unit = { hd ->
            flow<Unit> {
                while (true) {
                    hd.updateSafely {
                        val timeNow = timeStamper.nowUTC
                        Timber.v("Running timeout check (now=%s): %s", timeNow, values)
                        val timeoutLimit = appConfigProvider.currentConfig.first().overallDetectionTimeout
                        mutate {
                            values.filter { it.isCalculating }.toList().forEach {
                                if (timeNow.isAfter(it.startedAt.plus(timeoutLimit))) {
                                    Timber.w("Calculation timeout on %s", it)
                                    this[it.identifier] = it.copy(
                                        finishedAt = timeStamper.nowUTC,
                                        result = Result.TIMEOUT
                                    )
                                }
                            }
                        }
                    }

                    delay(TIMEOUT_CHECK_INTERVALL.millis)
                }
            }.launchIn(scope + dispatcherProvider.Default)
        }

        HotDataFlow(
            loggingTag = TAG,
            scope = scope,
            coroutineContext = dispatcherProvider.Default,
            startValueProvider = { storage.load() }
        ).also {
            setupAutoSave(it)
            setupTimeoutEnforcer(it)
        }
    }

    override val calculations: Flow<Map<String, TrackedExposureDetection>> by lazy { detectionStates.data }

    override fun trackNewExposureDetection(identifier: String) {
        Timber.i("trackNewExposureDetection(token=%s)", identifier)
        detectionStates.updateSafely {
            mutate {
                this[identifier] = TrackedExposureDetection(
                    identifier = identifier,
                    startedAt = timeStamper.nowUTC
                )
            }
        }
    }

    override fun finishExposureDetection(identifier: String, result: Result) {
        Timber.i("finishExposureDetection(token=%s, result=%s)", identifier, result)
        detectionStates.updateSafely {
            mutate {
                val existing = this[identifier]
                if (existing != null) {
                    if (existing.result == Result.TIMEOUT) {
                        Timber.w("Detection is late, already hit timeout, still updating.")
                    } else if (existing.result != null) {
                        Timber.e("Duplicate callback. Result is already set for detection!")
                    }
                    this[identifier] = existing.copy(
                        result = result,
                        finishedAt = timeStamper.nowUTC
                    )
                } else {
                    Timber.e(
                        "Unknown detection finished (token=%s, result=%s)",
                        identifier,
                        result
                    )
                    this[identifier] = TrackedExposureDetection(
                        identifier = identifier,
                        result = result,
                        startedAt = timeStamper.nowUTC,
                        finishedAt = timeStamper.nowUTC
                    )
                }
                val toKeep = entries
                    .sortedByDescending { it.value.startedAt } // Keep newest
                    .subList(0, min(entries.size, MAX_ENTRY_SIZE))
                    .map { it.key }
                entries.removeAll { entry ->
                    val remove = !toKeep.contains(entry.key)
                    if (remove) Timber.v("Removing stale entry: %s", entry)
                    remove
                }
            }
        }
    }

    override fun clear() {
        Timber.i("clear()")
        detectionStates.updateSafely {
            emptyMap()
        }
    }

    companion object {
        private const val TAG = "DefaultExposureDetectionTracker"
        private const val MAX_ENTRY_SIZE = 5
        private val TIMEOUT_CHECK_INTERVALL = Duration.standardMinutes(3)
    }
}
