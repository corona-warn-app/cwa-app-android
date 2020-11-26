package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

/**
 * Create a stateful flow, with the initial value of null, but never emits a null value.
 * Helper method to create a new flow without suspending and without initial value
 * The flow collector will just wait for the first value
 */
fun <T : Any> Flow<T>.shareLatest(
    tag: String? = null,
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(replayExpirationMillis = 0)
) = this
    .onStart {
        if (tag != null) Timber.tag(tag).v("shareLatest(...) start")
    }
    .onEach {
        if (tag != null) Timber.tag(tag).v("shareLatest(...) emission: %s", it)
    }
    .onCompletion {
        if (tag != null) Timber.tag(tag).v("shareLatest(...) completed.")
    }
    .catch {
        if (tag != null) Timber.tag(tag).w(it, "shareLatest(...) catch()!.")
        throw it
    }
    .stateIn(
        scope = scope,
        started = started,
        initialValue = null
    )
    .filterNotNull()

@Suppress("UNCHECKED_CAST", "LongParameterList")
inline fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R
): Flow<R> = combine(
    flow, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10, flow11
) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
        args[6] as T7,
        args[7] as T8,
        args[8] as T9,
        args[9] as T10,
        args[10] as T11
    )
}
