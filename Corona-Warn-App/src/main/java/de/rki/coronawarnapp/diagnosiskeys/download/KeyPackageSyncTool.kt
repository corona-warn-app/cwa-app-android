package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
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
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val daySyncTool: DaySyncTool,
    private val hourSyncTool: HourSyncTool,
    private val syncSettings: KeyPackageSyncSettings,
    private val timeStamper: TimeStamper,
    private val networkStateProvider: NetworkStateProvider
) {

    suspend fun syncKeyFiles(
        wanted: List<LocationCode> = listOf(LocationCode("EUR"))
    ): Result {
        val targetLocations = keyServer.getLocationIndex().let { available ->
            available.filter { wanted.contains(it) }.apply {
                Timber.tag(TAG).v("Available=%s; Wanted=%s; Intersect=%s", available, wanted, this)
            }
        }

        cleanUpStaleLocation(targetLocations)

        val hasSyncedDays = runDaySync(targetLocations)

        val isMeteredConnection = networkStateProvider.networkState.first().isMeteredConnection
        Timber.tag(TAG).d("Checking hour sync... (isMetered=%b)", isMeteredConnection)
        if (!isMeteredConnection || syncSettings.allowMeteredConnections.value) {
            runHourSync(targetLocations)
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

        return Result(
            availableKeys = availableKeys,
            wasDaySyncSucccessful = hasSyncedDays
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

    private suspend fun runDaySync(locations: List<LocationCode>): Boolean {
        val lastDownload = syncSettings.lastDownloadDays.value
        Timber.d("Synchronizing available days (lastDownload=%s).", lastDownload)

        syncSettings.lastDownloadDays.update {
            KeyPackageSyncSettings.LastDownload(startedAt = timeStamper.nowUTC)
        }

        val successfulSync = daySyncTool.syncMissingDays(
            availableLocations = locations,
            forceSync = lastDownload == null || !lastDownload.successful
        )

        syncSettings.lastDownloadDays.update {
            if (it == null) {
                Timber.tag(TAG).e("lastDownloadDays is missing a download start!?")
                null
            } else {
                it.copy(finishedAt = timeStamper.nowUTC, successful = successfulSync)
            }
        }

        return successfulSync.also {
            Timber.tag(TAG).d("runDaySync(locations=%s): success=%b", locations, it)
        }
    }

    private suspend fun runHourSync(locations: List<LocationCode>): Boolean {
        val lastDownload = syncSettings.lastDownloadHours.value
        Timber.tag(TAG).d("Synchronizing available hours (lastDownload=%s).", lastDownload)

        syncSettings.lastDownloadHours.update {
            KeyPackageSyncSettings.LastDownload(
                startedAt = timeStamper.nowUTC
            )
        }

        val successfulSync = hourSyncTool.syncMissingHours(
            availableLocations = locations,
            forceSync = lastDownload == null || !lastDownload.successful
        )

        syncSettings.lastDownloadHours.update {
            if (it == null) {
                Timber.tag(TAG).e("lastDownloadHours is missing a download start!?")
                null
            } else {
                it.copy(finishedAt = timeStamper.nowUTC, successful = successfulSync)
            }
        }

        return successfulSync.also {
            Timber.tag(TAG).d("runHourSync(locations=%s): success=%b", locations, it)
        }
    }

    data class Result(
        val availableKeys: Collection<CachedKey>,
        val wasDaySyncSucccessful: Boolean
    )

    companion object {
        internal const val TAG = "KeySync"
    }
}
