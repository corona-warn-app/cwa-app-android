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
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
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
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

@Reusable
class HourPackageSyncTool @Inject constructor(
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

    internal suspend fun syncMissingHourPackages(
        targetLocations: List<LocationCode>,
        forceIndexLookup: Boolean
    ): SyncResult {
        Timber.tag(TAG).v("syncMissingHours(targetLocations=%s)", targetLocations)

        val downloadConfig: KeyDownloadConfig = configProvider.getAppConfig()
        val keysWereRevoked = revokeCachedKeys(downloadConfig.revokedHourPackages)

        val missingHours = targetLocations.mapNotNull {
            try {
                determineMissingHours(it, forceIndexLookup || keysWereRevoked)
            } catch (e: CwaServerError) {
                Timber.tag(TAG).i("missing hours sync failed due to network problems")
                return SyncResult(successful = false, newPackages = emptyList())
            }
        }
        if (missingHours.isEmpty()) {
            Timber.tag(TAG).i("There were no missing hours.")
            return SyncResult(successful = true, newPackages = emptyList())
        }

        Timber.tag(TAG).d("Downloading missing hours: %s", missingHours)
        requireStorageSpace(missingHours)

        val hourDownloads = launchDownloads(missingHours, downloadConfig)

        Timber.tag(TAG).d("Waiting for %d missing hour downloads.", hourDownloads.size)
        val downloadedHours = hourDownloads.awaitAll().filterNotNull().also {
            Timber.tag(TAG).v("Downloaded keyfile: %s", it.joinToString("\n"))
        }
        Timber.tag(TAG).i("Download success: ${downloadedHours.size}/${hourDownloads.size}")

        return SyncResult(
            successful = hourDownloads.size == downloadedHours.size,
            newPackages = downloadedHours
        )
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
        Timber.tag(TAG).d("Launching %d downloads.", downloads.size)

        return downloads.map { (location, day, missingHour) ->
            withContext(context = dispatcherProvider.IO) {
                launcher(location, day, missingHour)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun expectNewHourPackages(cachedHours: List<CachedKey>, now: Instant): Boolean {
        val today = now.atZone(ZoneOffset.UTC)
        val newestHour = cachedHours.maxOfOrNull { it.info.pkgDateTime }

        return today.minusHours(1).hour != newestHour?.hour || today.toLocalDate() != newestHour.toLocalDate()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun determineMissingHours(
        location: LocationCode,
        forceIndexLookup: Boolean
    ): LocationHours? {
        // existing or checked files -> no download needed
        val cachedHours = getCachedKeys(location, Type.LOCATION_HOUR)

        val now = timeStamper.nowUTC

        if (!forceIndexLookup && !expectNewHourPackages(cachedHours, now)) {
            Timber.tag(TAG).d("We don't expect new hour packages.")
            return null
        }

        val today = now.toLocalDateUtc()

        val availableHours = run {
            val hoursToday = try {
                keyServer.getHourIndex(location, today)
            } catch (e: NetworkConnectTimeoutException) {
                Timber.tag(TAG).e(e, "Failed to get today's hour due - not going to delete the cache.")
                throw e
            } catch (e: CwaUnknownHostException) {
                Timber.tag(TAG).e(e, "Failed to get today's hour index - unknown host.")
                throw e
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "failed to get today's hour index.")
                emptyList()
            }
            LocationHours(location, mapOf(today to hoursToday))
        }

        // If we have hours that are covered by a day, delete the hours
        val cachedDays = getCachedKeys(location, Type.LOCATION_DAY).map {
            it.info.day
        }.let { LocationDays(location, it) }
        val staleHours = cachedHours.findStaleData(listOf(cachedDays, availableHours))
        if (staleHours.isNotEmpty()) {
            Timber.tag(TAG).v("Deleting stale hours: %s", staleHours)
            keyCache.deleteInfoAndFile(staleHours.map { it.info })
        }

        // subtract key files that are not on the server any more
        val nonStaleHours = cachedHours.minus(staleHours)

        return availableHours.toMissingHours(nonStaleHours) // The missing hours
    }

    companion object {
        private const val TAG = "HourPackageSyncTool"
    }
}
