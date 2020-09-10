package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.KeyFileHeaderHook
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.storage.AppSettings
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.storage.InsufficientStorageException
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.debug.measureTimeMillisWithResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.LocalTime
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Downloads new or missing key files from the CDN
 */
@Reusable
class KeyFileDownloader @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val keyServer: DiagnosisKeyServer,
    private val keyCache: KeyCacheRepository,
    private val legacyKeyCache: LegacyKeyCacheMigration,
    private val settings: AppSettings
) {

    private suspend fun checkStorageSpace(countries: List<LocationCode>): DeviceStorage.CheckResult {
        val storageResult = deviceStorage.checkSpacePrivateStorage(
            // 512KB per day file, for 15 days, for each country ~ 65MB for 9 countries
            requiredBytes = countries.size * 15 * 512 * 1024L
        )
        Timber.tag(TAG).d("Storage check result: %s", storageResult)
        return storageResult
    }

    private suspend fun getCompletedKeyFiles(type: CachedKeyInfo.Type): List<CachedKeyInfo> {
        return keyCache
            .getEntriesForType(type)
            .filter { (keyInfo, file) ->
                val complete = keyInfo.isDownloadComplete
                val exists = file.exists()
                if (complete && !exists) {
                    Timber.tag(TAG).v("Incomplete download, will overwrite: %s", keyInfo)
                }
                // We overwrite not completed ones
                complete && exists
            }
            .map { it.first }
    }

    /**
     * Fetches all necessary Files from the Cached KeyFile Entries out of the [KeyCacheRepository] and
     * adds to that all open Files currently available from the Server.
     *
     * Assumptions made about the implementation:
     * - the app initializes with an empty cache and draws in every available data set in the beginning
     * - the difference can only work properly if the date from the device is synchronized through the net
     * - the difference in timezone is taken into account by using UTC in the Conversion from the Date to Server format
     *
     * @return list of all files from both the cache and the diff query
     */
    suspend fun asyncFetchKeyFiles(wantedCountries: List<LocationCode>): List<File> =
        withContext(Dispatchers.IO) {
            val availableCountries = keyServer.getCountryIndex()
            val filteredCountries = availableCountries.filter { wantedCountries.contains(it) }
            Timber.tag(TAG).v(
                "Available=%s; Wanted=%s; Intersect=%s",
                availableCountries, wantedCountries, filteredCountries
            )

            val storageResult = checkStorageSpace(filteredCountries)
            if (!storageResult.isSpaceAvailable) throw InsufficientStorageException(storageResult)

            val availableKeys =
                if (CWADebug.isDebugBuildOrMode && settings.isLast3HourModeEnabled) {
                    syncMissing3Hours(filteredCountries, DEBUG_HOUR_LIMIT)
                    keyCache.getEntriesForType(CachedKeyInfo.Type.COUNTRY_HOUR)
                } else {
                    syncMissingDays(filteredCountries)
                    keyCache.getEntriesForType(CachedKeyInfo.Type.COUNTRY_DAY)
                }

            return@withContext availableKeys
                .filter { it.first.isDownloadComplete && it.second.exists() }
                .mapNotNull { (keyInfo, path) ->
                    if (!path.exists()) {
                        Timber.tag(TAG).w("Missing keyfile for : %s", keyInfo)
                        null
                    } else {
                        path
                    }
                }
                .also { Timber.tag(TAG).d("Returning %d available keyfiles", it.size) }
        }

    private suspend fun determineMissingDays(availableCountries: List<LocationCode>): List<CountryDays> {
        val availableDays = availableCountries.map {
            val days = keyServer.getDayIndex(it)
            CountryDays(it, days)
        }

        val cachedDays = getCompletedKeyFiles(CachedKeyInfo.Type.COUNTRY_DAY)

        // All cached files that are no longer on the server are considered stale
        val staleDays = cachedDays.filter { cachedDay ->
            val availableCountry = availableDays.singleOrNull {
                it.country == cachedDay.location
            }
            if (availableCountry == null) {
                Timber.tag(TAG).w("Unknown location %s, assuming stale day.", cachedDay.location)
                return@filter true // It's stale
            }

            availableCountry.dayData.none { date ->
                cachedDay.day == date
            }
        }

        if (staleDays.isNotEmpty()) {
            Timber.tag(TAG).v("Deleting stale days: %s", staleDays)
            keyCache.delete(staleDays)
        }

        val nonStaleDays = cachedDays.minus(staleDays)

        // The missing days
        return availableDays.mapNotNull { it.toMissingDays(nonStaleDays) }
    }

    /**
     * Fetches files given by serverDates by respecting countries
     * @param availableCountries pair of dates per country code
     */
    private suspend fun syncMissingDays(
        availableCountries: List<LocationCode>
    ) = withContext(Dispatchers.IO) {
        val countriesWithMissingDays = determineMissingDays(availableCountries)

        Timber.tag(TAG).d("Downloading missing days: %s", countriesWithMissingDays)
        val batchDownloadStart = System.currentTimeMillis()
        val dayDownloads = countriesWithMissingDays
            .flatMap { country ->
                country.dayData.map { dayDate -> country to dayDate }
            }
            .map { (countryWrapper, dayDate) ->
                async {
                    val (keyInfo, path) = keyCache.createCacheEntry(
                        location = countryWrapper.country,
                        dayIdentifier = dayDate,
                        hourIdentifier = null,
                        type = CachedKeyInfo.Type.COUNTRY_DAY
                    )

                    return@async try {
                        downloadKeyFile(keyInfo, path)
                        keyInfo to path
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Download failed: %s", keyInfo)
                        null
                    }
                }
            }

        Timber.tag(TAG).d("Waiting for %d missing day downloads.", dayDownloads.size)
        // execute the query plan
        val downloadedDays = dayDownloads.awaitAll().filterNotNull()

        Timber.tag(TAG).d(
            "Batch download (%d files) finished in %dms",
            dayDownloads.size,
            (System.currentTimeMillis() - batchDownloadStart)
        )

        downloadedDays.map { (keyInfo, path) ->
            Timber.tag(TAG).v("Downloaded keyfile: %s to %s", keyInfo, path)
            path
        }

        return@withContext
    }

    private suspend fun determineMissingHours(
        availableCountries: List<LocationCode>,
        itemLimit: Int
    ): List<CountryHours> {

        val availableHours = availableCountries.flatMap { location ->
            var remainingItems = itemLimit
            // Descending because we go backwards newest -> oldest
            keyServer.getDayIndex(location).sortedDescending().mapNotNull { day ->
                // Limit reached, return null (filtered out) instead of new CountryHours object
                if (remainingItems <= 0) return@mapNotNull null

                val hoursForDate = mutableListOf<LocalTime>()
                for (hour in keyServer.getHourIndex(location, day).sortedDescending()) {
                    if (remainingItems <= 0) break
                    remainingItems--
                    hoursForDate.add(hour)
                }

                CountryHours(location, mapOf(day to hoursForDate))
            }
        }

        val cachedHours = getCompletedKeyFiles(CachedKeyInfo.Type.COUNTRY_HOUR)

        // All cached files that are no longer on the server are considered stale
        val staleHours = cachedHours.filter { cachedHour ->
            val availCountry = availableHours.singleOrNull {
                it.country == cachedHour.location && it.hourData.containsKey(cachedHour.day)
            }
            if (availCountry == null) {
                Timber.w("Unknown location %s, assuming stale hour.", cachedHour.location)
                return@filter true // It's stale
            }

            val availableDay = availCountry.hourData[cachedHour.day]
            if (availableDay == null) {
                Timber.d("Unknown day %s, assuming stale hour.", cachedHour.location)
                return@filter true // It's stale
            }

            availableDay.none { time ->
                cachedHour.hour == time
            }
        }

        if (staleHours.isNotEmpty()) {
            Timber.tag(TAG).v("Deleting stale hours: %s", staleHours)
            keyCache.delete(staleHours)
        }

        val nonStaleHours = cachedHours.minus(staleHours)

        // The missing hours
        return availableHours.mapNotNull { it.toMissingHours(nonStaleHours) }
    }

    /**
     * Fetches files given by serverDates by respecting countries
     * @param availableCountries pair of dates per country code
     * @param hourItemLimit how many hours to go back
     */
    private suspend fun syncMissing3Hours(
        availableCountries: List<LocationCode>,
        hourItemLimit: Int
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v(
            "asyncHandleLast3HoursFilesFetch(availableCountries=%s, hourLimit=%d)",
            availableCountries, hourItemLimit
        )
        val missingHours = determineMissingHours(availableCountries, hourItemLimit)
        Timber.tag(TAG).d("Downloading missing hours: %s", missingHours)

        val hourDownloads = missingHours.flatMap { country ->
            country.hourData.flatMap { (day, missingHours) ->
                missingHours.map { missingHour ->
                    async {
                        val (keyInfo, path) = keyCache.createCacheEntry(
                            location = country.country,
                            dayIdentifier = day,
                            hourIdentifier = missingHour,
                            type = CachedKeyInfo.Type.COUNTRY_HOUR
                        )

                        return@async try {
                            downloadKeyFile(keyInfo, path)
                            keyInfo to path
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Download failed: %s", keyInfo)
                            keyCache.delete(listOf(keyInfo))
                            null
                        }
                    }
                }
            }
        }

        Timber.tag(TAG).d("Waiting for %d missing hour downloads.", hourDownloads.size)
        val downloadedHours = hourDownloads.awaitAll().filterNotNull()

        downloadedHours.map { (keyInfo, path) ->
            Timber.tag(TAG).d("Downloaded keyfile: %s to %s", keyInfo, path)
            path
        }

        return@withContext
    }

    private suspend fun downloadKeyFile(keyInfo: CachedKeyInfo, saveTo: File) {
        val validation = KeyFileHeaderHook { headers ->
            // tryMigration returns true when a file was migrated, meaning, no download necessary
            return@KeyFileHeaderHook !legacyKeyCache.tryMigration(
                headers.getPayloadChecksumMD5(),
                saveTo
            )
        }

        keyServer.downloadKeyFile(
            locationCode = keyInfo.location,
            day = keyInfo.day,
            hour = keyInfo.hour,
            saveTo = saveTo,
            headerHook = validation
        )

        Timber.tag(TAG).v("Dowwnload finished: %s -> %s", keyInfo, saveTo)

        val (downloadedMD5, duration) = measureTimeMillisWithResult { saveTo.hashToMD5() }
        Timber.tag(TAG).v("Hashed to MD5 in %dms: %s", duration, saveTo)

        keyCache.markKeyComplete(keyInfo, downloadedMD5)
    }

    companion object {
        private val TAG: String? = KeyFileDownloader::class.simpleName
        private const val DEBUG_HOUR_LIMIT = 3
    }
}
