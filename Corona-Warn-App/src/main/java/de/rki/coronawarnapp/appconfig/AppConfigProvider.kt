package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor(
    private val source: AppConfigSource,
    dispatcherProvider: DispatcherProvider,
    @AppScope private val scope: CoroutineScope,
    private val timeStamper: TimeStamper
) {

    private val configHolder = HotDataFlow(
        loggingTag = "AppConfigProvider",
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily
    ) {
        source.retrieveConfig()
    }

    fun getConfig(tryUpdate: Boolean = false): Flow<ConfigData> = configHolder.data.onStart {
        val now = timeStamper.nowUTC
        configHolder.updateBlocking {
            if (tryUpdate || now.isAfter(updatedAt.plus(CACHE_TIMEOUT))) {
                source.retrieveConfig()
            } else {
                this
            }
        }.also { emit(it) }
    }.distinctUntilChanged()

    fun forceUpdate() {
        Timber.tag(TAG).v("forceUpdate()")
        configHolder.updateSafely {
            source.clear()

            source.retrieveConfig()
        }
    }

    suspend fun getAppConfig(): ConfigData = getConfig().first()

    companion object {
        private const val TAG = "AppConfigProvider"
        private val CACHE_TIMEOUT = Duration.standardMinutes(3)
    }
}
