package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val source: AppConfigSource,
    private val dispatcherProvider: DispatcherProvider,
    @AppScope private val scope: CoroutineScope
) {

    private val mutex = Mutex()
    private val currentConfigInternal = MutableStateFlow<ConfigData?>(null)

    val currentConfig: Flow<ConfigData?> = currentConfigInternal

    suspend fun clear() = mutex.withLock {
        Timber.tag(TAG).v("clear()")
        source.clear()
        currentConfigInternal.value = null
    }

    suspend fun getAppConfig(): ConfigData = mutex.withLock {
        Timber.tag(TAG).v("getAppConfig()")
        withContext(context = scope.coroutineContext + dispatcherProvider.IO) {
            source.retrieveConfig().also {
                currentConfigInternal.emit(it)
            }
        }
    }

    companion object {
        private const val TAG = "AppConfigProvider"
    }
}
