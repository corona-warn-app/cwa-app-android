package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.DownloadInfo
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.AppSettings
import de.rki.coronawarnapp.storage.DeviceStorage
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

    private suspend fun requireStorageSpace(data: List<CountryData>): DeviceStorage.CheckResult {
        val requiredBytes = data.fold(0L) { acc, item ->
            acc + item.approximateSizeInBytes
        }
        Timber.d("%dB are required for %s", requiredBytes, data)
        return deviceStorage.requireSpacePrivateStorage(requiredBytes).also {
            Timber.tag(TAG).d("Storage check result: %s", it)
        }
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

            val availableKeys =
                if (settings.isLast3HourModeEnabled) {
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
                        Timber.tag(TAG).v("Providing available key: %s", keyInfo)
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

        val staleDays = getStale(cachedDays, availableDays)

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

        requireStorageSpace(countriesWithMissingDays)

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

                    return@async downloadKeyFile(keyInfo, path)
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
            val indexWithToday = keyServer.getDayIndex(location).let {
                val lastDayInIndex = it.maxOrNull()
                Timber.tag(TAG).v("Last day in index: %s", lastDayInIndex)
                if (lastDayInIndex != null) {
                    it.plus(lastDayInIndex.plusDays(1))
                } else {
                    it
                }
            }
            Timber.tag(TAG).v("Day index with (fake) today entry: %s", indexWithToday)

            indexWithToday.sortedDescending().mapNotNull { day ->
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

        val staleHours = getStale(cachedHours, availableHours)

        if (staleHours.isNotEmpty()) {
            Timber.tag(TAG).v("Deleting stale hours: %s", staleHours)
            keyCache.delete(staleHours)
        }

        val nonStaleHours = cachedHours.minus(staleHours)

        // The missing hours
        return availableHours.mapNotNull { it.toMissingHours(nonStaleHours) }
    }

    // All cached files that are no longer on the server are considered stale
    private fun getStale(
        cachedKeys: List<CachedKeyInfo>,
        availableData: List<CountryData>
    ): List<CachedKeyInfo> = cachedKeys.filter { cachedKey ->
        val availableCountry = availableData
            .filter { it.country == cachedKey.location }
            .singleOrNull {
                when (cachedKey.type) {
                    CachedKeyInfo.Type.COUNTRY_DAY -> true
                    CachedKeyInfo.Type.COUNTRY_HOUR -> {
                        it as CountryHours
                        it.hourData.containsKey(cachedKey.day)
                    }
                }
            }
        if (availableCountry == null) {
            Timber.w("Unknown location %s, assuming stale hour.", cachedKey.location)
            return@filter true // It's stale
        }

        when (cachedKey.type) {
            CachedKeyInfo.Type.COUNTRY_DAY -> {
                availableCountry as CountryDays
                availableCountry.dayData.none { date ->
                    cachedKey.day == date
                }
            }
            CachedKeyInfo.Type.COUNTRY_HOUR -> {
                availableCountry as CountryHours
                val availableDay = availableCountry.hourData[cachedKey.day]
                if (availableDay == null) {
                    Timber.d("Unknown day %s, assuming stale hour.", cachedKey.location)
                    return@filter true // It's stale
                }

                availableDay.none { time ->
                    cachedKey.hour == time
                }
            }
        }
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

        requireStorageSpace(missingHours)

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

                        return@async downloadKeyFile(keyInfo, path)
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

    private suspend fun downloadKeyFile(
        keyInfo: CachedKeyInfo,
        saveTo: File
    ): Pair<CachedKeyInfo, File>? = try {
        val preconditionHook: suspend (DownloadInfo) -> Boolean =
            { downloadInfo ->
                val continueDownload = !legacyKeyCache.tryMigration(
                    downloadInfo.serverMD5, saveTo
                )
                continueDownload // Continue download if no migration happened
            }

        val dlInfo = keyServer.downloadKeyFile(
            locationCode = keyInfo.location,
            day = keyInfo.day,
            hour = keyInfo.hour,
            saveTo = saveTo,
            precondition = preconditionHook
        )

        Timber.tag(TAG).v("Dowwnload finished: %s -> %s", keyInfo, saveTo)

        keyCache.markKeyComplete(keyInfo, dlInfo.serverMD5 ?: dlInfo.localMD5!!)
        keyInfo to saveTo
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Download failed: %s", keyInfo)
        keyCache.delete(listOf(keyInfo))
        null
    }

    companion object {
        private val TAG: String? = KeyFileDownloader::class.simpleName
        private const val DEBUG_HOUR_LIMIT = 3

        // Daymode: ~512KB per day, ~14 days
        // Hourmode: ~20KB per hour, 24 hours, also ~512KB
        private val EXPECTED_STORAGE_PER_COUNTRY =
            TimeVariables.getDefaultRetentionPeriodInDays() * 512 * 1024L
    }
}
