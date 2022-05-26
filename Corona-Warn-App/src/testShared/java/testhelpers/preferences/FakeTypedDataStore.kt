package testhelpers.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.updateAndGet
import timber.log.Timber

internal class FakeTypedDataStore<T>(
    private val defaultValue: T,
    private val shouldLog: Boolean = false
) : DataStore<T> {

    private val internalFlow = MutableStateFlow(defaultValue)

    override val data: Flow<T> = internalFlow
        .onEach { log("Emitting %s", it) }

    override suspend fun updateData(transform: suspend (t: T) -> T): T = internalFlow.updateAndGet { oldData ->
        transform(oldData).also { log("Updated data %s -> %s", oldData, it) }
    }

    fun reset() {
        log("Resetting to defaultValue=%s", defaultValue)
        internalFlow.value = defaultValue
    }

    private fun log(msg: String, vararg args: Any?) {
        if (shouldLog) Timber.v(msg, *args)
    }
}
