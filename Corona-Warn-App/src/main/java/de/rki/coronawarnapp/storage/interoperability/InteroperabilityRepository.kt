package de.rki.coronawarnapp.storage.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.storage.LocalData
import java.util.Locale

object InteroperabilityRepository {
    private val TAG: String? = InteroperabilityRepository::class.simpleName

    /**
     * LiveData that can be used in a ViewModel for selected country changes
     */
    val selectedCountryCodes = MutableLiveData<List<String>>()

    val isAllCountriesSelected = MutableLiveData<Boolean>(false)

    fun getSelectedCountryCodes(): List<String> =
        LocalData.countryCodes ?: listOf()

    /**
     * Refresh selected country codes state
     */
    fun refreshSelectedCountryCodes() {
        val codes = getSelectedCountryCodes()
        selectedCountryCodes.value = codes
    }

    fun refreshAllCountriesSelected() {
        isAllCountriesSelected.value = LocalData.isAllCountriesSelected
    }

    /**
     * Updates all countries given
     */
    fun overwriteSelectedCountries(countryCodes: List<String>, selected: Boolean) {
        var codes = listOf<String>()

        if (selected) {
            codes = countryCodes
        }

        setIsAllCountriesSelected(selected)

        LocalData.countryCodes = codes
        selectedCountryCodes.value = LocalData.countryCodes
    }

    fun setIsAllCountriesSelected(selected: Boolean) {
        LocalData.isAllCountriesSelected = selected
        refreshAllCountriesSelected()
    }

    /**
     * Update selected countries by one country
     */
    fun updateSelectedCountryCodes(
        countryCode: String,
        selected: Boolean = true
    ) {
        val selectedCodes = getSelectedCountryCodes()
        val updatedCountryCodesList = updateCountryCodesList(selectedCodes, countryCode, selected)
        LocalData.countryCodes = updatedCountryCodesList
        selectedCountryCodes.value = updatedCountryCodesList
    }

    private fun updateCountryCodesList(
        countryCodes: List<String>,
        countryCode: String,
        selected: Boolean
    ): List<String> {
        return if (selected) {
            countryCodes + countryCode
        } else {
            countryCodes - countryCode
        }
    }
}
