package de.rki.coronawarnapp.statistics.source

import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.util.HashExtensions.toHexString
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
import timber.log.Timber
import java.time.Duration
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
            stopTimeoutMillis = Duration.ofSeconds(5).toMillis(),
            replayExpirationMillis = 0
        )
    ) {
        try {
            val cachedValues = fromCache()
            if (cachedValues == null) {
                triggerUpdate()
                StatisticsData()
            } else {
                cachedValues
            }
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
        val rawData = localCache.load()
        rawData?.let { it -> parser.parse(it) }?.also {
            Timber.tag(TAG).d("Parsed from cache: %s", it)
            if (!it.isDataAvailable) {
                Timber.tag(TAG).w("RawData: %s", rawData.toHexString())
            }
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
            if (!it.isDataAvailable) {
                Timber.tag(TAG).w("RawData: %s", rawData.toHexString())
            }
            localCache.save(rawData)
        }
    }

    fun triggerUpdate() {
        Timber.tag(TAG).d("triggerUpdate()")
        statisticsData.updateAsync(
            onUpdate = { fromServer() },
            onError = { Timber.tag(TAG).e(it, "Failed to update statistics.") }
        )
    }

    companion object {
        const val TAG = "StatisticsProvider"
    }
}
