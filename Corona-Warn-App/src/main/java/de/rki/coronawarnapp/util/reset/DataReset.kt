package de.rki.coronawarnapp.util.reset

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DataReset @Inject constructor(
    private val resettableDataProvider: Provider<Set<@JvmSuppressWildcards Resettable>>
) {

    private val mutex = Mutex()
    private val resettableData get() = resettableDataProvider.get()

    suspend fun clearAllLocalData() = mutex.withLock {
        withContext(NonCancellable) {
            Timber.w("CWA LOCAL DATA DELETION INITIATED.")

            resettableData.forEach { data ->
                runCatching { data.reset() }
                    .onFailure { Timber.e(it, "Failed to reset %s", data::class.java.simpleName) }
            }

            Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
        }
    }
}
