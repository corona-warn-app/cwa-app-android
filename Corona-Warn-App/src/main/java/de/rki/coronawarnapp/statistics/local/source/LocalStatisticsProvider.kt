package de.rki.coronawarnapp.statistics.local.source

import de.rki.coronawarnapp.statistics.LocalStatisticsData
import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
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
            stopTimeoutMillis = Duration.ofSeconds(5).toMillis(),
            replayExpirationMillis = 0
        )
    ) {
        try {
            fetchCacheFirst()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get data from server.")
            emptyList()
        }
    }

    val current: Flow<LocalStatisticsData> = localStatisticsData.data.map { localStatsList ->
        val groupedStats = localStatsList.reduceOrNull { acc, localStatisticsData ->
            LocalStatisticsData(acc.items + localStatisticsData.items)
        } ?: LocalStatisticsData()

        groupedStats.copy(
            items = groupedStats.items
                .distinctBy { it.selectedLocation.uniqueID }
                .sortedBy { it.selectedLocation.addedAt }
                .reversed()
        )
    }

    private suspend fun fetchCacheFirst(): List<LocalStatisticsData> {
        Timber.tag(TAG).d("fromCache()")

        val activePackages = localStatisticsConfigStorage.activePackages.first()

        val cacheResults = activePackages.map { fromCache(it) }

        if (cacheResults.contains(null)) {
            triggerUpdate()
        }

        return cacheResults.map { it ?: LocalStatisticsData() }
    }

    private fun fromCache(forState: FederalStateToPackageId): LocalStatisticsData? = try {
        Timber.tag(TAG).d("fromCache(%s)", forState)

        localStatisticsCache.load(forState)?.let { localStatisticsParser.parse(it) }?.also {
            Timber.tag(TAG).d("Parsed from cache: %s", it)
        }
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to parse cached data.")
        null
    }

    private suspend fun fromServer(): List<LocalStatisticsData> {
        Timber.tag(TAG).d("fromServer()")

        val activePackages = localStatisticsConfigStorage.activePackages.first()

        return activePackages.map { fromServer(it) }
    }

    private suspend fun fromServer(forState: FederalStateToPackageId): LocalStatisticsData {
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

    companion object {
        const val TAG = "LocalStatisticsProvider"
    }
}
