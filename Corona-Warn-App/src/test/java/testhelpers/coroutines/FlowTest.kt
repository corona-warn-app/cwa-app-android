package testhelpers.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

suspend fun <T> Flow<T>.test(
    tag: String? = null,
    scope: CoroutineScope
): TestCollector<T> = TestCollector(this, scope, tag ?: "FlowTest").apply {
    ensureSetup()
}

class TestCollector<T>(
    private val flow: Flow<T>,
    private val scope: CoroutineScope,
    private val tag: String

) {
    private val mutex = Mutex()
    private var isSetupDone = false
    private val collectedValues = mutableListOf<T>()
    private var error: Throwable? = null
    private lateinit var job: Job

    suspend fun ensureSetup() = mutex.withLock {
        if (isSetupDone) return@withLock
        isSetupDone = true

        Timber.tag(tag).v("Setting up.")
        flow
            .onEach {
                Timber.tag(tag).v("Collecting: %s", it)
                collectedValues.add(it)
            }
            .onCompletion {
                Timber.tag(tag).d("Final.")
            }
            .catch { e ->
                Timber.tag(tag).w(e, "Caught error.")
                error = e
            }
            .launchIn(scope)
            .also { job = it }
    }

    suspend fun awaitFinal() = apply {
        ensureSetup()
        job.join()
    }

    suspend fun assertNoErrors() = apply {
        ensureSetup()
        awaitFinal()
        require(error == null) { "Error was not null: $error" }
    }

    suspend fun values(): List<T> {
        ensureSetup()
        return collectedValues
    }

    suspend fun cancel() {
        ensureSetup()
        job.cancelAndJoin()
    }
}
