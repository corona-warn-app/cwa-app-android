package de.rki.coronawarnapp.nearby.modules.calculationtracker

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.coroutine.HotData
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultCalculationTracker @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val storage: CalculationTrackerStorage,
    private val timeStamper: TimeStamper
) : CalculationTracker {

    private val calculationStates: HotData<Map<String, Calculation>> by lazy {
        HotData(
            loggingTag = TAG,
            scope = scope,
            coroutineContext = dispatcherProvider.Default,
            startValueProvider = { storage.load() }
        ).also { hotData ->
            hotData.data
                .onStart { Timber.v("Observing calculation changes.") }
                .onEach { Timber.v("New calculations: %s", it) }
                .onEach { storage.save(it) }
                .launchIn(scope = scope + dispatcherProvider.Default)
        }
    }
    override val calculations: Flow<Map<String, Calculation>> by lazy { calculationStates.data }

    init {
        Timber.v("init()")
    }

    override fun trackNewCalaculation(token: String) {
        Timber.i("trackNewCalaculation(token=%s)", token)
        calculationStates.updateSafely {
            mutate {
                this[token] = Calculation(
                    token = token,
                    state = Calculation.State.CALCULATING,
                    startedAt = timeStamper.nowUTC
                )
            }
        }
    }

    override fun finishCalculation(token: String, result: Calculation.Result) {
        Timber.i("finishCalculation(token=%s, result=%s)", token, result)
        calculationStates.updateSafely {
            mutate {
                val existing = this[token]
                if (existing != null) {
                    this[token] = existing.copy(
                        result = result,
                        state = Calculation.State.DONE,
                        finishedAt = timeStamper.nowUTC
                    )
                } else {
                    Timber.e("Unknown calculation finished (token=%s, result=%s)", token, result)
                    this[token] = Calculation(
                        token = token,
                        state = Calculation.State.DONE,
                        result = result,
                        startedAt = timeStamper.nowUTC,
                        finishedAt = timeStamper.nowUTC
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "DefaultCalculationTracker"
    }
}
