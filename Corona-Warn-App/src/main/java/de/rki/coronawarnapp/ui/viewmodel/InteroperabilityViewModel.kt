package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import kotlinx.coroutines.runBlocking

/**
 * ViewModel for everything Interoperability related
 *
 * @see InteroperabilityRepository
 */
class InteroperabilityViewModel : ViewModel() {
    val selectedCountryCodes = InteroperabilityRepository.selectedCountryCodes
    val isAllCountriesSelected = InteroperabilityRepository.isAllCountriesSelected

    fun refreshInteroperability() {
        refreshAllCountriesSelected()
        refreshSelectedCountryCodes()
    }

    fun refreshSelectedCountryCodes() {
        InteroperabilityRepository.refreshSelectedCountryCodes()
    }

    fun refreshAllCountriesSelected() {
        InteroperabilityRepository.refreshAllCountriesSelected()
    }

    fun overwriteSelectedCountries(selected: Boolean) {
        runBlocking {
            val countries =
                ApplicationConfigurationService
                    .asyncRetrieveApplicationConfiguration().supportedCountriesList
            InteroperabilityRepository.overwriteSelectedCountries(countries, selected)
        }
    }

    fun setIsAllCountriesSelected(selected: Boolean) {
        InteroperabilityRepository.setIsAllCountriesSelected(selected)
    }

    fun updateSelectedCountryCodes(
        countryCode: String,
        selected: Boolean = true
    ) {
        InteroperabilityRepository.updateSelectedCountryCodes(countryCode, selected)
    }
}
