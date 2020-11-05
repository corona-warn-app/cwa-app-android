package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyPackageSyncTool @Inject constructor(
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val daySyncTool: DaySyncTool,
    private val hourSyncTool: HourSyncTool,
    private val localData: KeyDownloadLocalData,
    private val timeStamper: TimeStamper
) {

    suspend fun syncKeyFiles(
        wantedLocations: List<LocationCode> = listOf(LocationCode("EUR"))
    ): Result {
        val lastDownload = localData.lastDownload.value
        localData.lastDownload.update {
            KeyDownloadLocalData.LastDownload(
                startedAt = timeStamper.nowUTC,
            )
        }

        val availableCountries = keyServer.getLocationIndex()
        val filteredCountries = availableCountries.filter { wantedLocations.contains(it) }
        Timber.tag(TAG).v(
            "Available=%s; Wanted=%s; Intersect=%s",
            availableCountries, wantedLocations, filteredCountries
        )

        Timber.d("Synchronizing available days.")
        val syncedDaysSuccessfully = daySyncTool.syncMissingDays(
            availableLocations = filteredCountries,
            forceSync = lastDownload == null || !lastDownload.successful
        )
        Timber.d("Synchronizing available hours.")
        val syncedHoursSuccessfully = hourSyncTool.syncMissingHours(
            availableLocations = filteredCountries,
            forceSync = lastDownload == null || !lastDownload.successful
        )

        if (syncedDaysSuccessfully && syncedHoursSuccessfully) {
            localData.lastDownload.update {
                if (it == null) {
                    Timber.tag(TAG).e("Finished a download that didn't start? Missing `LastDownload`.")
                    null
                } else {
                    it.copy(
                        finishedAt = timeStamper.nowUTC,
                        successful = true
                    )
                }
            }
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
            wasDaySyncSucccessful = syncedDaysSuccessfully
        )
    }

    data class Result(
        val availableKeys: Collection<CachedKey>,
        val wasDaySyncSucccessful: Boolean
    )

    companion object {
        internal const val TAG = "KeyPackageSync"
    }
}
