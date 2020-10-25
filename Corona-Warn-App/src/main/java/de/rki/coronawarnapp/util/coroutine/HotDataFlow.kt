package de.rki.coronawarnapp.util.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
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

    private val internalFlow = channelFlow {
        var currentValue = startValueProvider().also {
            Timber.tag(tag).v("startValue=%s", it)
            send(it)
        }

        updateActions.collect { updateAction ->
            currentValue = updateAction(currentValue).also {
                currentValue = it
                send(it)
            }
        }
    }

    val data: Flow<T> = internalFlow
        .onStart { Timber.tag(tag).v("internal onStart") }
//        .onEach { Timber.tag(tag).v("internal onEach: %s", it) }
        .catch {
            Timber.tag(tag).e(it, "internal Error")
            throw it
        }
        .onCompletion { Timber.tag(tag).v("internal onCompletion") }
        .buffer(capacity = Int.MAX_VALUE)
        .shareIn(
            scope = scope + coroutineContext,
            replay = 1,
            started = sharingBehavior
        )
        .buffer(capacity = Int.MAX_VALUE)
        .mapNotNull { it }

    fun updateSafely(update: suspend T.() -> T) = updateActions.tryEmit(update)
}
