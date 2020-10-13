package testhelpers.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
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
    private var isFinal = false

    suspend fun ensureSetup() = mutex.withLock {
        if (isSetupDone) return@withLock
        isSetupDone = true
        try {
            Timber.tag(tag).v("Setting up.")
            scope.launch {
                flow
                    .onEach {
                        Timber.tag(tag).v("Collecting: %s", it)
                        collectedValues.add(it)
                    }
                    .toList()
            }
        } catch (e: Exception) {
            Timber.tag(tag).w(e, "Caught error.")
            error = e
        } finally {
            Timber.tag(tag).d("Final.")
            isFinal = true
        }
    }

    suspend fun awaitFinal() = apply {
        ensureSetup()
        while (!isFinal) {
            yield()
        }
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
}
