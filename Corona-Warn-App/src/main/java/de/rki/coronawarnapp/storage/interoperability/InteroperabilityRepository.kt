package de.rki.coronawarnapp.storage.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.runBlocking
import java.util.Locale

object InteroperabilityRepository {
    private val TAG: String? = InteroperabilityRepository::class.simpleName

    fun interoperabilityWasShown(): Boolean = LocalData.interoperabilityWasShown()

    fun saveInteroperabilityUsed() {
        LocalData.saveInteroperabilityUsed()
    }

    /**
     * Gets all countries from @see ApplicationConfigurationService.asyncRetrieveApplicationConfiguration
     * and filters out the CURRENT_COUNTRY from @see DiagnosisKeyConstants. Also changes every country code
     * to lower case
     */
    fun getAllCountries(): List<String> {
        return runBlocking {
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                .supportedCountriesList
                ?.map { it.toLowerCase(Locale.ROOT) } ?: listOf()
        }
    }
}
