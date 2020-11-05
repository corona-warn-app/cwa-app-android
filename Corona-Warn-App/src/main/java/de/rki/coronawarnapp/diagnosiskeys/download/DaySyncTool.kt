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
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DaySyncTool @Inject constructor(
    deviceStorage: DeviceStorage,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val downloadTool: DownloadTool,
    private val timeStamper: TimeStamper,
    private val configProvider: AppConfigProvider,
    private val dispatcherProvider: DispatcherProvider
) : BaseSyncTool(
    keyCache = keyCache,
    deviceStorage = deviceStorage,
    tag = TAG
) {

    /**
     * returns true if the sync was successful
     * and false if not all files have been synced
     */
    internal suspend fun syncMissingDays(
        availableLocations: List<LocationCode>,
        forceSync: Boolean
    ): Boolean {
        Timber.tag(TAG).v("syncMissingDays(availableCountries=%s)", availableLocations)

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()
        invalidateCachedKeys(downloadConfig.invalidDayETags)

        val missingDays = availableLocations.mapNotNull {
            determineMissingDays(it, forceSync)
        }
        if (missingDays.isEmpty()) {
            Timber.tag(TAG).i("There were no missing days.")
            return true
        }

        Timber.tag(TAG).d("Downloading missing days: %s", missingDays)
        requireStorageSpace(missingDays)

        val downloads = launchDownloads(missingDays)

        Timber.tag(TAG).d("Waiting for %d missing day downloads.", downloads.size)
        val downloadedDays = downloads.awaitAll().filterNotNull().also {
            Timber.tag(TAG).v("Downloaded keyfile: %s", it.joinToString("\n"))
        }

        return downloads.size == downloadedDays.size
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun expectNewDayPackages(cachedDays: List<CachedKey>): Boolean {
        val yesterday = timeStamper.nowUTC.toLocalDate().minusDays(1)
        val newestDay = cachedDays.map { it.info.createdAt }.maxOrNull()?.toLocalDate()

        return yesterday != newestDay
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineMissingDays(location: LocationCode, forceSync: Boolean): LocationDays? {
        val cachedDays = getCompletedCachedKeys(location, Type.LOCATION_DAY)

        if (!forceSync && !expectNewDayPackages(cachedDays)) return null

        val availableDays = LocationDays(location, keyServer.getDayIndex(location))

        val staleDays = cachedDays.findStaleData(listOf(availableDays))

        if (staleDays.isNotEmpty()) {
            Timber.tag(TAG).d("Deleting stale days (loation=%s): %s", location, staleDays)
            keyCache.delete(staleDays.map { it.info })
        }

        val nonStaleDays = cachedDays.minus(staleDays)

        return availableDays.toMissingDays(nonStaleDays) // The missing days
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun launchDownloads(missingDayData: Collection<LocationDays>): Collection<Deferred<CachedKey?>> {
        val launcher: CoroutineScope.(LocationDays, LocalDate) -> Deferred<CachedKey?> = { locationData, targetDay ->
            async {
                val cachedKey = keyCache.createCacheEntry(
                    location = locationData.location,
                    dayIdentifier = targetDay,
                    hourIdentifier = null,
                    type = Type.LOCATION_DAY
                )

                downloadTool.downloadKeyFile(cachedKey)
            }
        }

        return missingDayData
            .flatMap { location ->
                location.dayData.map { dayDate -> location to dayDate }
            }
            .map { (locationData, targetDay) ->
                withContext(context = dispatcherProvider.IO) {
                    launcher(locationData, targetDay)
                }
            }
    }

    companion object {
        private const val TAG = "${KeyPackageSyncTool.TAG}:DaySync"
    }
}
