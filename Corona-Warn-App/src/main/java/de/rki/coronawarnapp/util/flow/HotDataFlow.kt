package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * A thread safe flow that can be updated blockingly and async, with way to provide an initial (lazy) value.
 *
 * @param loggingTag will be prepended to logging tag, i.e. "$loggingTag:HD"
 * @param scope on which the update operations and callbacks will be executed on
 * @param coroutineContext used in combination with [scope]
 * @param sharingBehavior see [Flow.shareIn]
 * @param startValueProvider provides the first value, errors will be rethrown on [scope]
 */
class HotDataFlow<T : Any>(
    loggingTag: String,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    sharingBehavior: SharingStarted = SharingStarted.WhileSubscribed(),
    private val startValueProvider: suspend CoroutineScope.() -> T
) {
    private val tag = "$loggingTag:HD"

    init {
        Timber.tag(tag).v("init()")
    }

    private val updateActions = MutableSharedFlow<Update<T>>(
        replay = Int.MAX_VALUE,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val valueGuard = Mutex()

    private val internalProducer: Flow<State<T>> = channelFlow {
        var currentValue = valueGuard.withLock {
            startValueProvider().also {
                Timber.tag(tag).v("startValue=%s", it)

                val initializer = Update<T>(onError = null, onModify = { it })

                send(State(value = it, updatedBy = initializer))
            }
        }
        Timber.tag(tag).v("startValue=%s", currentValue)

        updateActions
            .onCompletion {
                Timber.tag(tag).v("updateActions onCompletion -> resetReplayCache()")
                updateActions.resetReplayCache()
            }
            .collect { update ->
                currentValue = valueGuard.withLock {
                    try {
                        update.onModify(currentValue).also {
                            send(State(value = it, updatedBy = update))
                        }
                    } catch (e: Exception) {
                        Timber.tag(tag).v(e, "Data modifying failed (hasErrorHandler=${update.onError != null})")
                        if (update.onError != null) {
                            update.onError.invoke(e)
                        } else {
                            send(State(value = currentValue, error = e, updatedBy = update))
                        }
                        currentValue
                    }
                }
            }

        Timber.tag(tag).v("internal channelFlow finished.")
    }

    private val internalFlow = internalProducer
        .onStart { Timber.tag(tag).v("Internal onStart") }
        .onCompletion { err ->
            when {
                err is CancellationException -> Timber.tag(tag).d("internal onCompletion due to cancelation")
                err != null -> Timber.tag(tag).e(err, "internal onCompletion due to error")
                else -> Timber.tag(tag).v("internal onCompletion")
            }
        }
        .shareIn(
            scope = scope + coroutineContext,
            replay = 1,
            started = sharingBehavior
        )

    val data: Flow<T> = internalFlow
        .map { it.value }
        .distinctUntilChanged()

    /**
     * Non blocking update method.
     * Gets executed on the scope and context this instance was initialized with.
     *
     * @param onError if you don't provide this, and exception in [onUpdate] will the scope passed to this class
     */
    fun updateAsync(
        onError: (suspend (Exception) -> Unit) = { throw it },
        onUpdate: suspend T.() -> T,
    ): Boolean {
        val update: Update<T> = Update(
            onModify = onUpdate,
            onError = onError
        )
        return updateActions.tryEmit(update)
    }

    /**
     * Blocking update method
     * Gets executed on the scope and context this instance was initialized with.
     * Waiting will happen on the callers scope.
     *
     * Any errors that occured during [action] will be rethrown by this method.
     */
    suspend fun updateBlocking(action: suspend T.() -> T): T {
        val update: Update<T> = Update(onModify = action)
        updateActions.tryEmit(update)

        Timber.tag(tag).v("Waiting for update.")
        val ourUpdate = internalFlow.first { it.updatedBy == update }

        ourUpdate.error?.let { throw it }

        return ourUpdate.value
    }

    private data class Update<T>(
        val onModify: suspend T.() -> T,
        val onError: (suspend (Exception) -> Unit)? = null,
    )

    private data class State<T>(
        val value: T,
        val error: Exception? = null,
        val updatedBy: Update<T>,
    )
}
