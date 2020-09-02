package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.util.CachedKeyFileHolder.generateCacheKeyFromString

data class CountryUtils(val country: String, val dates: Collection<String>) {

    fun isDateEntryCacheMissing(formattedDate: String, cache: List<KeyCacheEntity>) = !cache
        .map { date -> date.id }
        .contains(getURLForDay(formattedDate).generateCacheKeyFromString())

    /**
     * Gets the correct URL String for querying a day bucket
     *
     * @param country the country of the formattedDate
     * @param formattedDate the formatted date
     */
    fun getURLForDay(formattedDate: String) =
        "${DiagnosisKeyConstants.AVAILABLE_COUNTRIES_URL}/$country/${DiagnosisKeyConstants.DATE}/$formattedDate"

    /**
     * Gets the correct URL String for querying an hour bucket
     *
     * @param country the country of the date and hour
     * @param formattedDate the formatted date for the hour bucket request
     * @param formattedHour the formatted hour
     */
    fun getURLForHour(formattedDate: String, formattedHour: String) =
        "${getURLForDay(formattedDate)}/${DiagnosisKeyConstants.HOUR}/$formattedHour"
}
