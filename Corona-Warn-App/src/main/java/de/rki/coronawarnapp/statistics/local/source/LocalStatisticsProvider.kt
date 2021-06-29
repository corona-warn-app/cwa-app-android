package de.rki.coronawarnapp.statistics.local.source

import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsProvider @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val server: LocalStatisticsServer,
    private val localStatisticsCache: LocalStatisticsCache,
    private val localStatisticsConfigStorage: LocalStatisticsConfigStorage,
    private val localStatisticsParser: LocalStatisticsParser,
    dispatcherProvider: DispatcherProvider,
) {

    private val localStatisticsData = HotDataFlow(
        loggingTag = TAG,
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        )
    ) {
        try {
            triggerUpdate()
            fetchCacheFirst()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get data from server.")
            emptyList()
        }
    }

    val current: Flow<List<StatisticsData>> = localStatisticsData.data

    private fun fetchCacheFirst(): List<StatisticsData> {
        Timber.tag(TAG).d("fromCache()")

        val targetedStates = localStatisticsConfigStorage.activeStates.value

        return targetedStates.map { fromCache(it) ?: StatisticsData() }
    }

    private fun fromCache(forState: FederalStateToPackageId): StatisticsData? = try {
        Timber.tag(TAG).d("fromCache(%s)", forState)

        localStatisticsCache.load(forState)?.let { localStatisticsParser.parse(it) }?.also {
            Timber.tag(TAG).d("Parsed from cache: %s", it)
        }
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to parse cached data.")
        null
    }

    private suspend fun fromServer(): List<StatisticsData> {
        Timber.tag(TAG).d("fromServer()")

        val targetedStates = localStatisticsConfigStorage.activeStates.value

        return targetedStates.map { fromServer(it) }
    }

    private suspend fun fromServer(forState: FederalStateToPackageId): StatisticsData {
        Timber.tag(TAG).d("fromServer(%s)", forState)

        val rawData = server.getRawLocalStatistics(forState)
        return localStatisticsParser.parse(rawData).also {
            Timber.tag(TAG).d("Parsed from server: %s", it)
            localStatisticsCache.save(forState, rawData)
        }
    }

    fun triggerUpdate() {
        Timber.tag(TAG).d("triggerUpdate()")
        localStatisticsData.updateAsync(
            onUpdate = { fromServer() },
            onError = { Timber.tag(TAG).e(it, "Failed to update statistics.") }
        )
    }

    fun clear() {
        Timber.d("clear()")

        server.clear()
        localStatisticsCache.clearAll()
    }

    companion object {
        const val TAG = "LocalStatisticsProvider"
    }
}
