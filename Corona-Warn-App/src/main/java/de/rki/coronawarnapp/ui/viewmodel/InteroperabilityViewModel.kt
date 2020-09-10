package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository

/**
 * ViewModel for everything Interoperability related
 *
 * @see InteroperabilityRepository
 */
class InteroperabilityViewModel : ViewModel() {
    val selectedCountryCodes = InteroperabilityRepository.selectedCountryCodes
    val isAllCountriesSelected = InteroperabilityRepository.isAllCountriesSelected
    val allCountries = InteroperabilityRepository.getAllCountries()

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
        InteroperabilityRepository.overwriteSelectedCountries(allCountries, selected)
    }

    fun updateSelectedCountryCodes(
        countryCode: String,
        selected: Boolean = true
    ) {
        InteroperabilityRepository.updateSelectedCountryCodes(countryCode, selected)

        // Disable all countries selected if user deselects a country
        if (!selected && InteroperabilityRepository.isAllCountriesSelected.value == false) {
            InteroperabilityRepository.setIsAllCountriesSelected(false)
            return
        }

        // check if all countries are selected now to set all countries selected to true
        val allCountriesSelected =
            selectedCountryCodes.value?.containsAll(allCountries) ?: false
        if (allCountriesSelected) {
            InteroperabilityRepository.setIsAllCountriesSelected(true)
        }
    }
}
