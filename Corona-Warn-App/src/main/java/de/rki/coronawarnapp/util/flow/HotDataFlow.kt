package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
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

class HotDataFlow<T : Any>(
    loggingTag: String,
    scope: CoroutineScope,
    coroutineContext: CoroutineContext = scope.coroutineContext,
    sharingBehavior: SharingStarted = SharingStarted.WhileSubscribed(),
    forwardException: Boolean = true,
    private val startValueProvider: suspend CoroutineScope.() -> T
) {
    private val tag = "$loggingTag:HD"

    init {
        Timber.tag(tag).v("init()")
    }

    private val updateActions = MutableSharedFlow<suspend (T) -> T>(
        replay = Int.MAX_VALUE,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val valueGuard = Mutex()

    private val internalProducer: Flow<Holder<T>> = channelFlow {
        var currentValue = valueGuard.withLock {
            startValueProvider().also {
                Timber.tag(tag).v("startValue=%s", it)
                val updatedBy: suspend T.() -> T = { it }
                send(Holder.Data(value = it, updatedBy = updatedBy))
            }
        }
        Timber.tag(tag).v("startValue=%s", currentValue)

        updateActions
            .onCompletion {
                Timber.tag(tag).v("updateActions onCompletion -> resetReplayCache()")
                updateActions.resetReplayCache()
            }
            .collect { updateAction ->
                currentValue = valueGuard.withLock {
                    updateAction(currentValue).also {
                        send(Holder.Data(value = it, updatedBy = updateAction))
                    }
                }
            }

        Timber.tag(tag).v("internal channelFlow finished.")
    }

    private val internalFlow = internalProducer
        .onStart { Timber.tag(tag).v("Internal onStart") }
        .catch {
            if (forwardException) {
                Timber.tag(tag).w(it, "Forwarding internal Error")
                // Wrap the error to get it past `sharedIn`
                emit(Holder.Error(error = it))
            } else {
                Timber.tag(tag).e(it, "Throwing internal Error")
                throw it
            }
        }
        .onCompletion { err ->
            if (err != null) Timber.tag(tag).w(err, "internal onCompletion due to error")
            else Timber.tag(tag).v("internal onCompletion")
        }
        .shareIn(
            scope = scope + coroutineContext,
            replay = 1,
            started = sharingBehavior
        )
        .map {
            when (it) {
                is Holder.Data<T> -> it
                is Holder.Error<T> -> throw it.error
            }
        }

    val data: Flow<T> = internalFlow.map { it.value }.distinctUntilChanged()

    fun updateSafely(update: suspend T.() -> T) = updateActions.tryEmit(update)

    suspend fun updateBlocking(update: suspend T.() -> T): T {
        updateActions.tryEmit(update)
        Timber.tag(tag).v("Waiting for update.")
        return internalFlow.first { it.updatedBy == update }.value
    }

    internal sealed class Holder<T> {
        data class Data<T>(
            val value: T,
            val updatedBy: suspend T.() -> T
        ) : Holder<T>()

        data class Error<T>(
            val error: Throwable
        ) : Holder<T>()
    }
}
