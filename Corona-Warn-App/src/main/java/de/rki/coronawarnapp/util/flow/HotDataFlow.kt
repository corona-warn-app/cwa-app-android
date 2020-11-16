package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
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
    coroutineContext: CoroutineContext = Dispatchers.Default,
    sharingBehavior: SharingStarted = SharingStarted.WhileSubscribed(),
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

    private val internalFlow = channelFlow {
        var currentValue = valueGuard.withLock {
            startValueProvider().also { send(it) }
        }
        Timber.tag(tag).v("startValue=%s", currentValue)

        updateActions
            .onCompletion {
                Timber.tag(tag).v("updateActions onCompletion -> resetReplayCache()")
                updateActions.resetReplayCache()
            }
            .collect { updateAction ->
                currentValue = valueGuard.withLock {
                    updateAction(currentValue).also { send(it) }
                }
            }

        Timber.tag(tag).v("internal channelFlow finished.")
    }

    val data: Flow<T> = internalFlow
        .distinctUntilChanged()
        .onStart { Timber.tag(tag).v("internal onStart") }
        .onCompletion { err ->
            if (err != null) Timber.tag(tag).w(err, "internal onCompletion due to error")
            else Timber.tag(tag).v("internal onCompletion")
        }
        .shareIn(
            scope = scope + coroutineContext,
            replay = 1,
            started = sharingBehavior
        )
        .mapNotNull { it }

    fun updateSafely(update: suspend T.() -> T) = updateActions.tryEmit(update)
}
