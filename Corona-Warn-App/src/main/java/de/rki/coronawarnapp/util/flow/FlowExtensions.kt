package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
) = onStart {
    if (tag != null) Timber.tag(tag).v("shareLatest(...) start")
}.onEach {
    if (tag != null) Timber.tag(tag).v("shareLatest(...) emission: %s", it)
}.onCompletion {
    if (tag != null) Timber.tag(tag).v("shareLatest(...) completed.")
}.catch {
    if (tag != null) Timber.tag(tag).w(it, "shareLatest(...) catch()!.")
    throw it
}.stateIn(
    scope = scope,
    started = started,
    initialValue = null
).filterNotNull()

/**
 * This is a shorthand for `scope.launch { flow.collectLatest(action) }`
 */
fun <T> Flow<T>.launchInLatest(
    scope: CoroutineScope,
    action: suspend (value: T) -> Unit
): Job = scope.launch {
    collectLatest(action) // tail-call
}
