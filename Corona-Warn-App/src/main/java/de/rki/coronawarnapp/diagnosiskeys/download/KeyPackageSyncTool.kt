package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyPackageSyncTool @Inject constructor(
    private val keyCache: KeyCacheRepository,
    private val dayPackageSyncTool: DayPackageSyncTool,
    private val hourPackageSyncTool: HourPackageSyncTool,
    private val syncSettings: KeyPackageSyncSettings,
    private val timeStamper: TimeStamper,
    private val networkStateProvider: NetworkStateProvider
) {

    suspend fun syncKeyFiles(
        wantedLocations: List<LocationCode> = listOf(LocationCode("EUR"))
    ): Result {
        cleanUpStaleLocation(wantedLocations)

        val daySyncResult = runDaySync(wantedLocations)

        val isMeteredConnection = networkStateProvider.networkState.first().isMeteredConnection

        val hourSyncResult = if (!isMeteredConnection) {
            Timber.tag(TAG).d("Running hour sync...")
            runHourSync(wantedLocations)
        } else {
            Timber.tag(TAG).d("Hour sync skipped, we are on a metered connection.")
            null
        }

        val availableKeys = keyCache.getAllCachedKeys()
            .filter { it.info.isDownloadComplete }
            .filter { (keyInfo, path) ->
                path.exists().also {
                    if (!it) Timber.tag(TAG).w("Missing keyfile for : %s", keyInfo)
                }
            }
            .also { Timber.tag(TAG).i("Returning %d available keyfiles", it.size) }
            .also { Timber.tag(TAG).d("Available keyfiles: %s", it.joinToString("\n")) }

        val newKeys = mutableListOf<CachedKey>()
        newKeys.addAll(daySyncResult.newPackages)
        hourSyncResult?.let { newKeys.addAll(it.newPackages) }

        return Result(
            availableKeys = availableKeys,
            newKeys = newKeys,
            wasDaySyncSucccessful = daySyncResult.successful
        )
    }

    private suspend fun cleanUpStaleLocation(acceptedLocations: List<LocationCode>) {
        Timber.tag(TAG).d("Checking for stale location, acceptable is: %s", acceptedLocations)

        val staleLocationData = keyCache.getAllCachedKeys()
            .map { it.info }
            .filter { !acceptedLocations.contains(it.location) }
        if (staleLocationData.isNotEmpty()) {
            Timber.tag(TAG).i("Deleting stale location data: %s", staleLocationData.joinToString("\n"))
            keyCache.delete(staleLocationData)
        } else {
            Timber.tag(TAG).d("No stale location data exists.")
        }
    }

    private suspend fun runDaySync(locations: List<LocationCode>): BaseKeyPackageSyncTool.SyncResult {
        val lastDownload = syncSettings.lastDownloadDays.value
        Timber.tag(TAG).d("Synchronizing available days (lastDownload=%s).", lastDownload)

        syncSettings.lastDownloadDays.update {
            KeyPackageSyncSettings.LastDownload(startedAt = timeStamper.nowUTC)
        }

        val syncResult = dayPackageSyncTool.syncMissingDayPackages(
            targetLocations = locations,
            forceIndexLookup = lastDownload == null || !lastDownload.successful
        )

        syncSettings.lastDownloadDays.update {
            if (it == null) {
                Timber.tag(TAG).e("lastDownloadDays is missing a download start!?")
                null
            } else {
                it.copy(finishedAt = timeStamper.nowUTC, successful = syncResult.successful)
            }
        }

        return syncResult.also {
            Timber.tag(TAG).d("runDaySync(locations=%s): syncResult=%s", locations, it)
        }
    }

    private suspend fun runHourSync(locations: List<LocationCode>): BaseKeyPackageSyncTool.SyncResult {
        val lastDownload = syncSettings.lastDownloadHours.value
        Timber.tag(TAG).d("Synchronizing available hours (lastDownload=%s).", lastDownload)

        syncSettings.lastDownloadHours.update {
            KeyPackageSyncSettings.LastDownload(
                startedAt = timeStamper.nowUTC
            )
        }

        val syncResult = hourPackageSyncTool.syncMissingHourPackages(
            targetLocations = locations,
            forceIndexLookup = lastDownload == null || !lastDownload.successful
        )

        syncSettings.lastDownloadHours.update {
            if (it == null) {
                Timber.tag(TAG).e("lastDownloadHours is missing a download start!?")
                null
            } else {
                it.copy(finishedAt = timeStamper.nowUTC, successful = syncResult.successful)
            }
        }

        return syncResult.also {
            Timber.tag(TAG).d("runHourSync(locations=%s): syncResult=%s", locations, it)
        }
    }

    data class Result(
        val availableKeys: Collection<CachedKey>,
        val newKeys: Collection<CachedKey>,
        val wasDaySyncSucccessful: Boolean
    )

    companion object {
        internal const val TAG = "KeySync"
    }
}
