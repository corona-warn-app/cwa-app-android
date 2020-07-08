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

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository.DateEntryType.DAY
import de.rki.coronawarnapp.util.CachedKeyFileHolder.asyncFetchFiles
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
    suspend fun asyncFetchFiles(currentDate: Date): List<File> = withContext(Dispatchers.IO) {
        checkForFreeSpace()
        val serverDates = getDatesFromServer()
        // TODO remove last3HourFetch before Release
        if (BuildConfig.FLAVOR != "device" && isLast3HourFetchEnabled()) {
            Timber.v("Last 3 Hours will be Fetched. Only use for Debugging!")
            val currentDateServerFormat = currentDate.toServerFormat()
            // just fetch the hours if the date is available
            if (serverDates.contains(currentDateServerFormat)) {
                return@withContext getLast3Hours(currentDate)
                    .map { getURLForHour(currentDate.toServerFormat(), it) }
                    .map { url ->
                        async {
                            return@async WebRequestBuilder.getInstance()
                                .asyncGetKeyFilesFromServer(url)
                        }
                    }.awaitAll()
            } else {
                throw IllegalStateException(
                    "you cannot use the last 3 hour mode if the date index " +
                            "does not contain any data for today"
                )
            }
        } else {
            val uuidListFromServer = serverDates
                .map { getURLForDay(it).generateCacheKeyFromString() }
            // queries will be executed after the "query plan" was set
            val deferredQueries: MutableCollection<Deferred<Any>> = mutableListOf()
            keyCache.deleteOutdatedEntries(uuidListFromServer)
            val missingDays = getMissingDaysFromDiff(serverDates)
            if (missingDays.isNotEmpty()) {
                // we have a date difference
                deferredQueries.addAll(
                    missingDays
                        .map { getURLForDay(it) }
                        .map { url -> async { url.createDayEntryForUrl() } }
                )
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
    }

    private fun checkForFreeSpace() = FileStorageHelper.checkFileStorageFreeSpace()

    /**
     * Calculates the missing days based on current missing entries in the cache
     */
    private suspend fun getMissingDaysFromDiff(datesFromServer: Collection<String>): List<String> {
        val cacheEntries = keyCache.getDates()
        return datesFromServer
            .also { Timber.d("${it.size} days from server") }
            .filter { it.dateEntryCacheMiss(cacheEntries) }
            .toList()
            .also { Timber.d("${it.size} missing days") }
    }

    /**
     * TODO remove before Release
     */
    private const val LATEST_HOURS_NEEDED = 3

    /**
     * Calculates the last 3 hours
     * TODO remove before Release
     */
    private suspend fun getLast3Hours(day: Date): List<String> = getHoursFromServer(day)
        .also { Timber.v("${it.size} hours from server, but only latest 3 hours needed") }
        .filter { TimeAndDateExtensions.getCurrentHourUTC() - LATEST_HOURS_NEEDED <= it.toInt() }
        .toList()
        .also { Timber.d("${it.size} missing hours") }

    /**
     * Determines whether a given String has an existing date cache entry under a unique name
     * given from the URL that is based on this String
     *
     * @param cache the given cache entries
     */
    private fun String.dateEntryCacheMiss(cache: List<KeyCacheEntity>) = !cache
        .map { date -> date.id }
        .contains(getURLForDay(this).generateCacheKeyFromString())

    /**
     * Creates a date entry in the Key Cache for a given String with a unique Key Name derived from the URL
     * and the URI of the downloaded File for that given key
     */
    private suspend fun String.createDayEntryForUrl() = keyCache.createEntry(
        this.generateCacheKeyFromString(),
        WebRequestBuilder.getInstance().asyncGetKeyFilesFromServer(this).toURI(),
        DAY
    )

    /**
     * Generates a unique key name (UUIDv3) for the cache entry based out of a string (e.g. an url)
     */
    private fun String.generateCacheKeyFromString() =
        "${UUID.nameUUIDFromBytes(this.toByteArray())}".also {
            Timber.v("$this mapped to cache entry $it")
        }

    /**
     * Gets the correct URL String for querying an hour bucket
     *
     * @param formattedDate the formatted date for the hour bucket request
     * @param formattedHour the formatted hour
     */
    private fun getURLForHour(formattedDate: String, formattedHour: String) =
        "${getURLForDay(formattedDate)}/${DiagnosisKeyConstants.HOUR}/$formattedHour"

    /**
     * Gets the correct URL String for querying a day bucket
     *
     * @param formattedDate the formatted date
     */
    private fun getURLForDay(formattedDate: String) =
        "${DiagnosisKeyConstants.AVAILABLE_DATES_URL}/$formattedDate"

    /**
     * Get all dates from server based as formatted dates
     */
    private suspend fun getDatesFromServer() =
        WebRequestBuilder.getInstance().asyncGetDateIndex()

    /**
     * Get all hours from server based as formatted dates
     */
    private suspend fun getHoursFromServer(day: Date) =
        WebRequestBuilder.getInstance().asyncGetHourIndex(day)

    /**
     * TODO remove before release
     */
    private fun isLast3HourFetchEnabled(): Boolean = LocalData.last3HoursMode()
}
