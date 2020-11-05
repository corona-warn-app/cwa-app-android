package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import org.joda.time.LocalDate
import org.joda.time.LocalTime

sealed class CountryData {

    abstract val country: LocationCode

    abstract val approximateSizeInBytes: Long
}

internal data class CountryDays(
    override val country: LocationCode,
    val dayData: Collection<LocalDate>
) : CountryData() {

    override val approximateSizeInBytes: Long by lazy {
        dayData.size * APPROX_DAY_SIZE
    }

    /**
     * Return a filtered list that contains all dates which are part of this wrapper, but not in the parameter.
     */
    fun getMissingDays(cachedKeys: List<CachedKey>): Collection<LocalDate>? {
        val cachedCountryDates = cachedKeys
            .filter { it.info.location == country }
            .map { it.info.day }

        return dayData.filter { date ->
            !cachedCountryDates.contains(date)
        }
    }

    /**
     * Create a new country object that only contains those elements,
     * that are part of this wrapper, but not in the cache.
     */
    fun toMissingDays(cachedKeys: List<CachedKey>): CountryDays? {
        val missingDays = this.getMissingDays(cachedKeys)
        if (missingDays == null || missingDays.isEmpty()) return null

        return CountryDays(this.country, missingDays)
    }

    companion object {
        // ~512KB
        private const val APPROX_DAY_SIZE = 512 * 1024L
    }
}

internal data class CountryHours(
    override val country: LocationCode,
    val hourData: Map<LocalDate, List<LocalTime>>
) : CountryData() {

    override val approximateSizeInBytes: Long by lazy {
        hourData.values.fold(0L) { acc, hoursForDay ->
            acc + hoursForDay.size * APPROX_HOUR_SIZE
        }
    }

    fun getMissingHours(cachedKeys: List<CachedKey>): Map<LocalDate, List<LocalTime>>? {
        val cachedHours = cachedKeys
            .filter { it.info.location == country }

        return hourData.mapNotNull { (day, dayHours) ->
            val missingHours = dayHours.filter { hour ->
                cachedHours.none { it.info.day == day && it.info.hour == hour }
            }
            if (missingHours.isEmpty()) null else day to missingHours
        }.toMap()
    }

    fun toMissingHours(cachedKeys: List<CachedKey>): CountryHours? {
        val missingHours = this.getMissingHours(cachedKeys)
        if (missingHours == null || missingHours.isEmpty()) return null

        return CountryHours(this.country, missingHours)
    }

    companion object {
        // ~22KB
        private const val APPROX_HOUR_SIZE = 22 * 1024L
    }
}
