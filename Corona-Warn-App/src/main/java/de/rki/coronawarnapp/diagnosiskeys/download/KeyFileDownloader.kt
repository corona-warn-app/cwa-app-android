package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.DiagnosisKeyServer
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.LegacyKeyCacheMigration
import de.rki.coronawarnapp.storage.AppSettings
import de.rki.coronawarnapp.storage.DeviceStorage
import de.rki.coronawarnapp.storage.InsufficientStorageException
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.HashExtensions.hashToMD5
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.debug.measureTimeMillisWithResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.joda.time.LocalDate
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Downloads new or missing key files from the CDN
 */
@Reusable
class KeyFileDownloader @Inject constructor(
    private val deviceStorage: DeviceStorage,
    private val diagnosisKeyServer: DiagnosisKeyServer,
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
     * @param currentDate the current date - if this is adjusted by the calendar, the cache is affected.
     * @return list of all files from both the cache and the diff query
     */
    suspend fun asyncFetchKeyFiles(
        currentDate: LocalDate,
        wantedCountries: List<LocationCode>
    ): List<File> = withContext(Dispatchers.IO) {
        val availableCountries = diagnosisKeyServer.getCountryIndex()
        val filteredCountries = availableCountries.filter { wantedCountries.contains(it) }
        Timber.tag(TAG).v(
            "Available=%s; Wanted=%s; Intersect=%s",
            availableCountries, wantedCountries, filteredCountries
        )

        val storageResult = checkStorageSpace(filteredCountries)
        if (!storageResult.isSpaceAvailable) throw InsufficientStorageException(storageResult)

        val availableKeys = if (CWADebug.isDebugBuildOrMode && settings.isLast3HourModeEnabled) {
            syncMissing3Hours(currentDate, filteredCountries, DEBUG_HOUR_LIMIT)
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
            val days = diagnosisKeyServer.getDayIndex(it)
            CountryDays(it, days)
        }

        val cachedDays = getCompletedKeyFiles(CachedKeyInfo.Type.COUNTRY_DAY)

        // All cached files that are no longer on the server are considered stale
        val staleDays = cachedDays.filter { cachedKeyFile ->
            val availableCountry = availableDays.singleOrNull {
                it.country == cachedKeyFile.location
            }
            if (availableCountry == null) {
                Timber.tag(TAG).w(
                    "Unknown location %s, assuming stale cache.",
                    cachedKeyFile.location
                )
                return@filter true // It's stale
            }

            availableCountry.dayData.none { date ->
                cachedKeyFile.day == date
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
        currentDate: LocalDate,
        availableCountries: List<LocationCode>,
        itemLimit: Int
    ): List<CountryHours> {
        // This is currently used for debugging, so we only fetch 3 hours

        val availableHours = availableCountries.map {
            val hoursForDate =
                diagnosisKeyServer.getHourIndex(it, currentDate).filter { availableHour ->
                    if (itemLimit < 0) {
                        true
                    } else {
                        TimeAndDateExtensions.getCurrentHourUTC() - itemLimit <= availableHour.hourOfDay
                    }
                }
            CountryHours(it, mapOf(currentDate to hoursForDate))
        }

        val cachedHours = getCompletedKeyFiles(CachedKeyInfo.Type.COUNTRY_HOUR)

        // All cached files that are no longer on the server are considered stale
        val staleHours = cachedHours.filter { cachedHour ->
            val availCountry = availableHours.singleOrNull {
                it.country == cachedHour.location
            }
            if (availCountry == null) {
                Timber.w("Unknown location %s, assuming stale.", cachedHour.location)
                return@filter true // It's stale
            }

            val availableDay = availCountry.hourData[cachedHour.day]
            if (availableDay == null) {
                Timber.d("Unknown day %s, assuming stale.", cachedHour.location)
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
     * @param currentDate base for where only dates within 3 hours before will be fetched
     * @param availableCountries pair of dates per country code
     */
    private suspend fun syncMissing3Hours(
        currentDate: LocalDate,
        availableCountries: List<LocationCode>,
        hourItemLimit: Int
    ) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).v(
            "asyncHandleLast3HoursFilesFetch(currentDate=%s, availableCountries=%s)",
            currentDate, availableCountries
        )
        val missingHours = determineMissingHours(currentDate, availableCountries, hourItemLimit)
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
        val validation = object : DiagnosisKeyServer.HeaderValidation {
            override suspend fun validate(headers: Headers): Boolean {
                var fileMD5 = headers.values("ETag").singleOrNull()
//                var fileMD5 = headers.values("x-amz-meta-cwa-hash-md5").singleOrNull()
//                if (fileMD5 == null) {
//                    headers.values("x-amz-meta-cwa-hash").singleOrNull()
//                }
//                if (fileMD5 == null) { // Fallback
//                    fileMD5 = headers.values("ETag").singleOrNull()
//                }
                fileMD5 = fileMD5?.removePrefix("\"")?.removeSuffix("\"")

                return !legacyKeyCache.tryMigration(fileMD5, saveTo)
            }
        }

        diagnosisKeyServer.downloadKeyFile(
            keyInfo.location,
            keyInfo.day,
            keyInfo.hour,
            saveTo,
            validation
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
