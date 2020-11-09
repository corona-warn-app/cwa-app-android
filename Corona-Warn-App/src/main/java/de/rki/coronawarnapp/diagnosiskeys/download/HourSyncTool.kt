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
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalTime
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class HourSyncTool @Inject constructor(
    deviceStorage: DeviceStorage,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val downloadTool: KeyDownloadTool,
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
    internal suspend fun syncMissingHours(
        availableLocations: List<LocationCode>,
        forceSync: Boolean
    ): Boolean {
        Timber.tag(TAG).v("syncMissingHours(availableCountries=%s)", availableLocations)

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()
        invalidateCachedKeys(downloadConfig.invalidHourEtags)

        val missingHours = availableLocations.mapNotNull {
            determineMissingHours(it, forceSync)
        }
        if (missingHours.isEmpty()) {
            Timber.tag(TAG).i("There were no missing hours.")
            return true
        }

        Timber.tag(TAG).d("Downloading missing hours: %s", missingHours)
        requireStorageSpace(missingHours)

        val hourDownloads = launchDownloads(missingHours, downloadConfig)

        Timber.tag(TAG).d("Waiting for %d missing hour downloads.", hourDownloads.size)
        val downloadedHours = hourDownloads.awaitAll().filterNotNull()

        downloadedHours.map { (keyInfo, path) ->
            Timber.tag(TAG).d("Downloaded keyfile: %s to %s", keyInfo, path)
            path
        }

        return hourDownloads.size == downloadedHours.size
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun launchDownloads(
        missingHours: Collection<LocationHours>,
        downloadConfig: KeyDownloadConfig
    ): Collection<Deferred<CachedKey?>> {
        val launcher: CoroutineScope.(LocationHours, LocalDate, LocalTime) -> Deferred<CachedKey?> =
            { locationData, targetDay, targetHour ->
                async {
                    val cachedKey = keyCache.createCacheEntry(
                        location = locationData.location,
                        dayIdentifier = targetDay,
                        hourIdentifier = targetHour,
                        type = Type.LOCATION_HOUR
                    )

                    try {
                        downloadTool.downloadKeyFile(cachedKey, downloadConfig)
                    } catch (e: Exception) {
                        // We can't throw otherwise it cancels the other downloads too (awaitAll)
                        null
                    }
                }
            }

        val downloads = missingHours
            .flatMap { location ->
                location.hourData.map { Triple(location, it.key, it.value) }
            }
            .flatMap { (location, day, hours) ->
                hours.map { Triple(location, day, it) }
            }
        Timber.tag(TAG).d("Launching %d downloads, with config: %s", downloads.size, downloadConfig)

        return downloads.map { (location, day, missingHour) ->
            withContext(context = dispatcherProvider.IO) {
                launcher(location, day, missingHour)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun expectNewHourPackages(cachedHours: List<CachedKey>): Boolean {
        val previousHour = timeStamper.nowUTC.toLocalTime().minusHours(1)
        val newestHour = cachedHours.map { it.info.createdAt }.maxOrNull()?.toLocalTime()

        return previousHour.hourOfDay != newestHour?.hourOfDay
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineMissingHours(location: LocationCode, forceSync: Boolean): LocationHours? {
        val cachedHours = getCompletedCachedKeys(location, Type.LOCATION_HOUR)

        if (!forceSync && !expectNewHourPackages(cachedHours)) return null

        val today = timeStamper.nowUTC.toLocalDate()

        val availableHours = keyServer.getHourIndex(location, today).let { todaysHours ->
            LocationHours(location, mapOf(today to todaysHours))
        }

        // If we have hours in covered by a day, delete the hours
        val cachedDays = getCompletedCachedKeys(location, Type.LOCATION_DAY).map {
            it.info.day
        }.let { LocationDays(location, it) }

        val staleHours = cachedHours.findStaleData(listOf(cachedDays, availableHours))

        if (staleHours.isNotEmpty()) {
            Timber.tag(TAG).v("Deleting stale hours: %s", staleHours)
            keyCache.delete(staleHours.map { it.info })
        }

        val nonStaleHours = cachedHours.minus(staleHours)

        return availableHours.toMissingHours(nonStaleHours) // The missing hours
    }

    companion object {
        private const val TAG = "${KeyPackageSyncTool.TAG}:HourSync"
    }
}
