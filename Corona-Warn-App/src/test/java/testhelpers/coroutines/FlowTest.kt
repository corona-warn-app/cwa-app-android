package testhelpers.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

fun <T> Flow<T>.test(
    tag: String? = null,
    startOnScope: CoroutineScope
): TestCollector<T> = test(tag ?: "FlowTest").start(scope = startOnScope)

fun <T> Flow<T>.test(
    tag: String? = null
): TestCollector<T> = TestCollector(this, tag ?: "FlowTest")

class TestCollector<T>(
    private val flow: Flow<T>,
    private val tag: String

) {
    private var error: Throwable? = null
    private lateinit var job: Job
    private val cache = MutableSharedFlow<T>(
        replay = Int.MAX_VALUE,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private var latestInternal: T? = null
    private val collectedValuesMutex = Mutex()
    private val collectedValues = mutableListOf<T>()

    var silent = false

    fun start(scope: CoroutineScope) = apply {
        flow
            .buffer(capacity = Int.MAX_VALUE)
            .onStart { Timber.tag(tag).v("Setting up.") }
            .onCompletion { Timber.tag(tag).d("Final.") }
            .onEach {
                collectedValuesMutex.withLock {
                    if (!silent) Timber.tag(tag).v("Collecting: %s", it)
                    latestInternal = it
                    collectedValues.add(it)
                    cache.emit(it)
                }
            }
            .catch { e ->
                Timber.tag(tag).w(e, "Caught error.")
                error = e
            }
            .launchIn(scope)
            .also { job = it }
    }

    fun emissions(): Flow<T> = cache

    val latestValue: T?
        get() = collectedValues.last()

    val latestValues: List<T>
        get() = collectedValues

    fun await(condition: (List<T>, T) -> Boolean): T = runBlocking {
        emissions().first {
            condition(collectedValues, it)
        }
    }

    suspend fun awaitFinal() = apply {
        try {
            job.join()
        } catch (e: Exception) {
            error = e
        }
    }

    suspend fun assertNoErrors() = apply {
        awaitFinal()
        require(error == null) { "Error was not null: $error" }
    }

    fun cancel() {
        runBlocking {
            job.cancelAndJoin()
        }
    }
}
