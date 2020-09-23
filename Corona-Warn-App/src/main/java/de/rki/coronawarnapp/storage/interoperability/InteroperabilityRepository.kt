package de.rki.coronawarnapp.storage.interoperability

import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteroperabilityRepository @Inject constructor() {

    fun saveInteroperabilityUsed() {
        LocalData.isInteroperabilityShownAtLeastOnce = true
    }

    /**
     * Gets all countries from @see ApplicationConfigurationService.asyncRetrieveApplicationConfiguration
     * Also changes every country code to lower case
     */
    fun getAllCountries(): List<String> {
        return runBlocking {
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                .supportedCountriesList
                ?.map { it.toLowerCase(Locale.ROOT) } ?: listOf()
        }
    }
}
