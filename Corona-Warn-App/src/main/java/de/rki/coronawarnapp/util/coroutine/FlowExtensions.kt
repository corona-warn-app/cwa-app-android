package de.rki.coronawarnapp.util.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
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
fun <T> Flow<T>.shareLatest(
    tag: String,
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed()
) = onStart { Timber.v("$tag FLOW start") }
    .onEach { Timber.v("$tag FLOW emission: %s", it) }
    .onCompletion { Timber.v("$tag FLOW completed.") }
    .stateIn(
        scope = scope,
        started = started,
        initialValue = null
    )
    .mapNotNull { it }
