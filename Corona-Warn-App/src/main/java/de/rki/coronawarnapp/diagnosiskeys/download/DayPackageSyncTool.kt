package de.rki.coronawarnapp.diagnosiskeys.download

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo.Type
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.pkgDateTime
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@Reusable
class DayPackageSyncTool @Inject constructor(
    deviceStorage: DeviceStorage,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val downloadTool: KeyDownloadTool,
    private val timeStamper: TimeStamper,
    private val configProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider
) : BaseKeyPackageSyncTool(
    keyCache = keyCache,
    deviceStorage = deviceStorage,
    tag = TAG
) {

    internal suspend fun syncMissingDayPackages(
        targetLocations: List<LocationCode>,
        forceIndexLookup: Boolean
    ): SyncResult {
        Timber.tag(TAG).v("syncMissingDays(targetLocations=%s)", targetLocations)

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()
        val keysWereRevoked = revokeCachedKeys(downloadConfig.revokedDayPackages)

        val missingDays = try {
            targetLocations.mapNotNull {
                determineMissingDayPackages(it, forceIndexLookup || keysWereRevoked)
            }
        } catch (e: CwaUnknownHostException) {
            Timber.tag(TAG).w(e, "Failed to sync with day index.")
            return SyncResult(successful = false, newPackages = emptyList())
        }

        if (missingDays.isEmpty()) {
            Timber.tag(TAG).i("There were no missing day packages.")
            return SyncResult(successful = true, newPackages = emptyList())
        }

        Timber.tag(TAG).d("Downloading missing day packages: %s", missingDays)
        requireStorageSpace(missingDays)

        val downloads = launchDownloads(missingDays, downloadConfig)

        Timber.tag(TAG).d("Waiting for %d missing day downloads.", downloads.size)
        val downloadedDays = downloads.awaitAll().filterNotNull().also {
            Timber.tag(TAG).v("Downloaded keyfile: %s", it.joinToString("\n"))
        }
        Timber.tag(TAG).i("Download success: ${downloadedDays.size}/${downloads.size}")

        return SyncResult(
            successful = downloads.size == downloadedDays.size,
            newPackages = downloadedDays
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun expectNewDayPackages(cachedDays: List<CachedKey>): Boolean {
        val yesterday = timeStamper.nowUTC.toLocalDateUtc().minusDays(1)
        val newestDay = cachedDays.maxOfOrNull { it.info.pkgDateTime }?.toLocalDate()

        return yesterday != newestDay
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineMissingDayPackages(
        location: LocationCode,
        forceIndexLookup: Boolean
    ): LocationDays? {
        // existing or checked files -> no download needed
        val cachedKeys = getCachedKeys(location, Type.LOCATION_DAY)

        if (!forceIndexLookup && !expectNewDayPackages(cachedKeys)) {
            Timber.tag(TAG).d("We don't expect new day packages.")
            return null
        }

        val availableDays = LocationDays(location, keyServer.getDayIndex(location))

        // remove files that are no longer available on the server
        val staleKeys = cachedKeys.findStaleData(listOf(availableDays))
        if (staleKeys.isNotEmpty()) {
            Timber.tag(TAG).d("Deleting stale days (loation=%s): %s", location, staleKeys)
            keyCache.deleteInfoAndFile(staleKeys.map { it.info })
        }
        val nonStaleCachedKeys = cachedKeys.minus(staleKeys)

        return availableDays.toMissingDays(nonStaleCachedKeys) // The missing days
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun launchDownloads(
        missingDayData: Collection<LocationDays>,
        downloadConfig: KeyDownloadConfig
    ): Collection<Deferred<CachedKey?>> {
        val launcher: CoroutineScope.(LocationDays, LocalDate) -> Deferred<CachedKey?> = { locationData, targetDay ->
            async {
                val cachedKey = keyCache.createCacheEntry(
                    location = locationData.location,
                    dayIdentifier = targetDay,
                    hourIdentifier = null,
                    type = Type.LOCATION_DAY
                )
                try {
                    downloadTool.downloadKeyFile(cachedKey, downloadConfig)
                } catch (e: Exception) {
                    // We can't throw otherwise it cancels the other downloads too (awaitAll)
                    null
                }
            }
        }
        val downloads = missingDayData.flatMap { location ->
            location.dayData.map { dayDate -> location to dayDate }
        }
        Timber.tag(TAG).d("Launching %d downloads.", downloads.size)

        return downloads.map { (locationData, targetDay) ->
            withContext(context = dispatcherProvider.IO) {
                launcher(locationData, targetDay)
            }
        }
    }

    companion object {
        private const val TAG = "DayPackageSyncTool"
    }
}
