/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository.DateEntryType.DAY
import de.rki.coronawarnapp.util.CachedKeyFileHolder.asyncFetchFiles
import de.rki.coronawarnapp.util.CachedKeyFileHolder.generateCacheKeyFromString
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Date
import java.util.UUID

/**
 * Singleton used for accessing key files via combining cached entries from existing files and new requests.
 * made explicitly with [asyncFetchFiles] in mind
 */
object CachedKeyFileHolder {
    private val TAG: String? = CachedKeyFileHolder::class.simpleName

    /**
     * the key cache instance used to store queried dates and hours
     */
    private val keyCache =
        KeyCacheRepository.getDateRepository(CoronaWarnApplication.getAppContext())

    /**
     * Fetches all necessary Files from the Cached KeyFile Entries out of the [KeyCacheRepository] and
     * adds to that all open Files currently available from the Server.
     *
     * Assumptions made about the implementation:
     * - the app initializes with an empty cache and draws in every available data set in the beginning
     * - the difference can only work properly if the date from the device is synchronized through the net
     * - the difference in timezone is taken into account by using UTC in the Conversion from the Date to Server format
     * - the missing days and hours are stored in one table as the actual stored data amount is low
     * - the underlying repository from the database has no error and is reliable as source of truth
     *
     * @param currentDate the current date - if this is adjusted by the calendar, the cache is affected.
     * @return list of all files from both the cache and the diff query
     */
    suspend fun asyncFetchFiles(
        currentDate: Date,
        countries: List<String>
    ): List<File> = withContext(Dispatchers.IO) {
        // Initiate key-cache folder needed for saving downloaded key files
        FileStorageHelper.initializeExportSubDirectory()

        checkForFreeSpace()

        // Build pair of country to date <Country, Date[]>
        val serverDates = getCountriesFromServer(countries).map {
            CountryDataWrapper(it, getDatesFromServer((it)))
        }

        if (CWADebug.isDebugBuildOrMode && LocalData.last3HoursMode()) {
            asyncHandleLast3HoursFilesFetch(currentDate, serverDates)
        } else {
            asyncHandleFilesFetch(serverDates)
        }
    }

    private fun checkForFreeSpace() = FileStorageHelper.checkFileStorageFreeSpace()

    /**
     * Fetches files given by serverDates by respecting countries
     * @param serverDates pair of dates per country code
     */
    private suspend fun asyncHandleFilesFetch(
        serverDates: List<CountryDataWrapper>
    ): List<File> = withContext(Dispatchers.IO) {
        // Build flat map of uuids from dates of countries
        val uuidListFromServer = serverDates.flatMap { countryWrapper ->
            countryWrapper.dates.map { date ->
                countryWrapper.getURLForDay(date)
            }
        }

        Timber.v("${uuidListFromServer.size} available dates from server for ${serverDates.size} countries")

        // queries will be executed after the "query plan" was set
        val deferredQueries: MutableCollection<Deferred<Any>> = mutableListOf()
        keyCache.deleteOutdatedEntries(uuidListFromServer)

        val countryWithMissingDays = getMissingDaysFromDiff(serverDates)
        if (countryWithMissingDays.isNotEmpty()) {
            // we have a date difference
            countryWithMissingDays
                .flatMap { country ->
                    country.dates.map { country.getURLForDay(it) }
                }
                .map { url ->
                    async {
                        keyCache.createEntry(
                            url.generateCacheKeyFromString(),
                            WebRequestBuilder.getInstance().asyncGetKeyFilesFromServer(url).toURI(),
                            DAY
                        )
                    }
                }
                .toList()
                .also { deferredQueries.addAll(it) }
        }

        // execute the query plan
        try {
            deferredQueries.awaitAll()
        } catch (e: Exception) {
            // For an error we clear the cache to try again
            keyCache.clear()
            throw e
        }

        keyCache.getFilesFromEntries()
            .also { it.forEach { file -> Timber.v("cached file:${file.path}") } }
    }

    /**
     * Fetches files given by serverDates by respecting countries
     * @param currentDate base for where only dates within 3 hours before will be fetched
     * @param serverDates pair of dates per country code
     */
    private suspend fun asyncHandleLast3HoursFilesFetch(
        currentDate: Date,
        serverDates: List<CountryDataWrapper>
    ): List<File> = withContext(Dispatchers.IO) {
        Timber.v("Last 3 Hours will be Fetched. Only use for Debugging!")
        val currentDateServerFormat = currentDate.toServerFormat()

        // just fetch the hours if the date is available
        // extend fetch for all dates in all countries provided in serverDates
        val packagesWithCurrentDate = serverDates
            .filter { countryWithDates ->
                countryWithDates.dates.contains(currentDateServerFormat)
            }

        if (packagesWithCurrentDate.isEmpty()) {
            throw IllegalStateException(
                "you cannot use the last 3 hour mode if the date index " +
                        "does not contain any data for today"
            )
        }

        return@withContext serverDates
            .flatMap { countryWithDates ->
                getLast3Hours(currentDate)
                    .map { hour ->
                        countryWithDates.getURLForHour(currentDate.toServerFormat(), hour)
                    }
                    .map { url ->
                        async {
                            return@async WebRequestBuilder.getInstance()
                                .asyncGetKeyFilesFromServer(url)
                        }
                    }
            }
            .awaitAll()
    }

    /**
     * Calculates the missing days based on current missing entries in the cache
     * with respect to all countries defined
     */
    private suspend fun getMissingDaysFromDiff(
        serverCountryData: Collection<CountryDataWrapper>
    ): Collection<CountryDataWrapper> {
        val cacheEntries = keyCache.getDates()
        return serverCountryData
            .also { Timber.d("Server country data: %s", it) }
            .map { availCountry ->
                CountryDataWrapper(
                    availCountry.country,
                    availCountry.getMissingDates(cacheEntries)
                )
            }
            .filter { countryData ->
                // Only return countries with missing dates.
                countryData.dates.isNotEmpty()
            }
            .also { Timber.d("Locally missing country data: %s", it) }
    }

    private const val LATEST_HOURS_NEEDED = 3

    /**
     * Calculates the last 3 hours
     */
    private suspend fun getLast3Hours(day: Date): List<String> = getHoursFromServer(day)
        .also { Timber.v("${it.size} hours from server, but only latest 3 hours needed") }
        .filter { TimeAndDateExtensions.getCurrentHourUTC() - LATEST_HOURS_NEEDED <= it.toInt() }
        .toList()
        .also { Timber.d("${it.size} missing hours") }

    /**
     * Generates a unique key name (UUIDv3) for the cache entry based out of a string (e.g. an url)
     */
    fun String.generateCacheKeyFromString() =
        "${UUID.nameUUIDFromBytes(this.toByteArray())}".also {
            Timber.v("$this mapped to cache entry $it")
        }

    /**
     * Get all dates of a country from server based as formatted dates
     *
     * @param country the country where the dates are got from
     */
    private suspend fun getDatesFromServer(country: String) =
        WebRequestBuilder.getInstance().asyncGetDateIndex(country)

    /**
     * Get all hours from server based as formatted dates
     */
    private suspend fun getHoursFromServer(day: Date) =
        WebRequestBuilder.getInstance().asyncGetHourIndex(day)

    /**
     * Get all countries from the server
     */
    private suspend fun getCountriesFromServer(countries: List<String>) =
        WebRequestBuilder.getInstance().asyncGetCountryIndex(countries)
}

internal data class CountryDataWrapper(val country: String, val dates: Collection<String>) {

    /**
     * Return a filtered list that contains all dates which are part of this wrapper, but not in the parameter.
     */
    fun getMissingDates(cachedKeys: Collection<KeyCacheEntity>): Collection<String> {
        val cacheIds = cachedKeys.map { it.id }

        return dates.filterNot { date ->
            val formattedDateUrl = getURLForDay(date)
            val cacheKeyFromUrl = formattedDateUrl.generateCacheKeyFromString()
            // If the cache doesn't contain our ID, it's a missing date
            cacheIds.contains(cacheKeyFromUrl)
        }
    }

    /**
     * Gets the correct URL String for querying a day bucket
     *
     * @param formattedDate the formatted date
     */
    fun getURLForDay(formattedDate: String) =
        "${DiagnosisKeyConstants.AVAILABLE_COUNTRIES_URL}/$country/${DiagnosisKeyConstants.DATE}/$formattedDate"

    /**
     * Gets the correct URL String for querying an hour bucket
     *
     * @param formattedDate the formatted date for the hour bucket request
     * @param formattedHour the formatted hour
     */
    fun getURLForHour(formattedDate: String, formattedHour: String) =
        "${getURLForDay(formattedDate)}/${DiagnosisKeyConstants.HOUR}/$formattedHour"
}
