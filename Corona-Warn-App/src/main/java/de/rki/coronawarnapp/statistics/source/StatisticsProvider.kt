package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsProvider @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val server: StatisticsServer,
    private val localCache: StatisticsCache,
    private val parser: StatisticsParser,
    foregroundState: ForegroundState,
    dispatcherProvider: DispatcherProvider
) {

    private val statisticsData = HotDataFlow(
        loggingTag = TAG,
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        )
    ) {
        try {
            fromCache() ?: fromServer()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get data from server.")
            StatisticsData()
        }
    }

    val current: Flow<StatisticsData> = statisticsData.data

    init {
        foregroundState.isInForeground
            .onEach {
                if (it) {
                    Timber.tag(TAG).d("App moved to foreground triggering statistics update.")
                    triggerUpdate()
                }
            }
            .catch { Timber.tag(TAG).e("Failed to trigger statistics update.") }
            .launchIn(scope)
    }

    private fun fromCache(): StatisticsData? = try {
        Timber.tag(TAG).d("fromCache()")
        localCache.load()?.let { parser.parse(it) }?.also {
            Timber.tag(TAG).d("Parsed from cache: %s", it)
        }
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to parse cached data.")
        null
    }

    private suspend fun fromServer(): StatisticsData {
        Timber.tag(TAG).d("fromServer()")
        val rawData = server.getRawStatistics()
        return parser.parse(rawData).also {
            Timber.tag(TAG).d("Parsed from server: %s", it)
            localCache.save(rawData)
        }
    }

    fun triggerUpdate() {
        Timber.tag(TAG).d("triggerUpdate()")
        statisticsData.updateSafely {
            try {
                fromServer()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to update statistics.")
                this@updateSafely // return previous data
            }
        }
    }

    fun clear() {
        Timber.d("clear()")
        server.clear()
        localCache.save(null)
    }

    companion object {
        const val TAG = "StatisticsProvider"
    }
}
