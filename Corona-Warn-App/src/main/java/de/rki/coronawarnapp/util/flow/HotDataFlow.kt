package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val internalProducer: Flow<DataHolder<T>> = channelFlow {
        var currentValue = startValueProvider().also {
            Timber.tag(tag).v("startValue=%s", it)
            val updatedBy: suspend T.() -> T = { it }
            send(DataHolder(value = it, updatedBy = updatedBy))
        }

        updateActions.collect { updateAction ->
            currentValue = updateAction(currentValue).also {
                currentValue = it
                send(DataHolder(value = it, updatedBy = updateAction))
            }
        }
    }

    private val internalFlow = internalProducer
        .onStart { Timber.tag(tag).v("internal onStart") }
        .catch {
            Timber.tag(tag).e(it, "internal Error")
            throw it
        }
        .onCompletion { Timber.tag(tag).v("internal onCompletion") }
        .shareIn(
            scope = scope + coroutineContext,
            replay = 1,
            started = sharingBehavior
        )
        .mapNotNull { it }

    val data: Flow<T> = internalFlow.map { it.value }.distinctUntilChanged()

    fun updateSafely(update: suspend T.() -> T) = updateActions.tryEmit(update)

    suspend fun updateBlocking(update: suspend T.() -> T): T {
        updateActions.tryEmit(update)
        Timber.tag(tag).v("Waiting for update.")
        return internalFlow.first {
            val target = it.updatedBy
            val desired = update
            Timber.tag(tag).v("Comparing %s with %s; match=%b", target, desired, target == desired)
            it.updatedBy == update
        }.value.also { Timber.tag(tag).v("Returning blocking update result: %s", it) }
    }

    internal data class DataHolder<T>(
        val value: T,
        val updatedBy: suspend T.() -> T
    )
}
