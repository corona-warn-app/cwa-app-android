package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.internal.AppConfigSource
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val appConfigSource: AppConfigSource,
    private val dispatcherProvider: DispatcherProvider,
    @AppScope private val scope: CoroutineScope
) : Resettable {

    private val configHolder = HotDataFlow(
        loggingTag = "AppConfigProvider",
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily
    ) {
        // Restore config-data without network calls because we don't want to block on initialization.
        appConfigSource.getConfigData(offlineMode = true)
    }

    val currentConfig: Flow<ConfigData> = configHolder.data

    suspend fun getAppConfig(): ConfigData {
        // Switch scope so the app config can't get canceled due to unsubscription,
        // we'd still like to have that new config in any case.
        val deferred = scope.async(context = dispatcherProvider.IO) {
            configHolder.updateBlocking {
                appConfigSource.getConfigData()
            }
        }
        return deferred.await()
    }

    override suspend fun reset() {
        Timber.tag(TAG).v("reset()")
        appConfigSource.clear()
    }

    companion object {
        private const val TAG = "AppConfigProvider"
    }
}

fun ConfigData.getSupportedCountries(): List<String> =
    supportedCountries.ifEmpty {
        Timber.w("Country list was empty, corrected")
        listOf("DE")
    }.also { Timber.i("Supported countries = $it") }
