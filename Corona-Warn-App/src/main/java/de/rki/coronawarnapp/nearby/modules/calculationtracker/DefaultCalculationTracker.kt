package de.rki.coronawarnapp.nearby.modules.calculationtracker

import de.rki.coronawarnapp.nearby.modules.calculationtracker.Calculation.Result
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
class DefaultCalculationTracker @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: CalculationTrackerStorage,
    private val timeStamper: TimeStamper
) : CalculationTracker {

    init {
        Timber.v("init()")
    }

    private val calculationStates: HotDataFlow<Map<String, Calculation>> by lazy {
        val setupAutoSave: (HotDataFlow<Map<String, Calculation>>) -> Unit = { hd ->
            hd.data
                .onStart { Timber.v("Observing calculation changes.") }
                .onEach { storage.save(it) }
                .launchIn(scope = scope + dispatcherProvider.Default)
        }

        val setupTimeoutEnforcer: (HotDataFlow<Map<String, Calculation>>) -> Unit = { hd ->
            flow<Unit> {
                while (true) {
                    hd.updateSafely {
                        Timber.v("Running timeout check on: %s", values)

                        val timeNow = timeStamper.nowUTC
                        Timber.v("Time now: %s", timeNow)

                        mutate {
                            values.filter { it.isCalculating }.toList().forEach {
                                if (timeNow.isAfter(it.startedAt.plus(TIMEOUT_LIMIT))) {
                                    Timber.w("Calculation timeout on %s", it)
                                    remove(it.identifier)
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

    override val calculations: Flow<Map<String, Calculation>> by lazy { calculationStates.data }

    override fun trackNewCalaculation(identifier: String) {
        Timber.i("trackNewCalaculation(token=%s)", identifier)
        calculationStates.updateSafely {
            mutate {
                this[identifier] = Calculation(
                    identifier = identifier,
                    startedAt = timeStamper.nowUTC
                )
            }
        }
    }

    override fun finishCalculation(identifier: String, result: Result) {
        Timber.i("finishCalculation(token=%s, result=%s)", identifier, result)
        calculationStates.updateSafely {
            mutate {
                val existing = this[identifier]
                if (existing != null) {
                    this[identifier] = existing.copy(
                        result = result,
                        finishedAt = timeStamper.nowUTC
                    )
                } else {
                    Timber.e(
                        "Unknown calculation finished (token=%s, result=%s)",
                        identifier,
                        result
                    )
                    this[identifier] = Calculation(
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

    companion object {
        private const val TAG = "DefaultCalculationTracker"
        private const val MAX_ENTRY_SIZE = 5
        private val TIMEOUT_CHECK_INTERVALL = Duration.standardMinutes(5)
        private val TIMEOUT_LIMIT = Duration.standardMinutes(15)
    }
}
