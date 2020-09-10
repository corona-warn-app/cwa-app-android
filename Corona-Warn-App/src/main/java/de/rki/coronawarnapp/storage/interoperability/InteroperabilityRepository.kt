package de.rki.coronawarnapp.storage.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.runBlocking
import java.util.Locale

object InteroperabilityRepository {
    private val TAG: String? = InteroperabilityRepository::class.simpleName

    /**
     * LiveData that can be used in a ViewModel for selected country changes
     */
    val selectedCountryCodes = MutableLiveData<List<String>>()

    val isAllCountriesSelected = MutableLiveData<Boolean>(false)

    private fun getSelectedCountryCodes(): List<String> =
        LocalData.countryCodes ?: listOf()

    /**
     * Gets all countries from @see ApplicationConfigurationService.asyncRetrieveApplicationConfiguration
     * and filters out the CURRENT_COUNTRY from @see DiagnosisKeyConstants. Also changes every country code
     * to lower case
     */
    fun getAllCountries(): List<String> {
        return runBlocking {
            ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                .supportedCountriesList
                ?.filter {
                    // Filter our CURRENT_COUNTRY because this country should always be used and it should
                    // not be able to disable it
                    it != DiagnosisKeyConstants.CURRENT_COUNTRY.toLowerCase(
                        Locale.ROOT
                    )
                }
                ?.map { it.toLowerCase(Locale.ROOT) } ?: listOf()
        }
    }

    /**
     * Refresh selected country codes state by localData
     */
    fun refreshSelectedCountryCodes() {
        val codes = getSelectedCountryCodes()
        selectedCountryCodes.value = codes
    }

    /**
     * Refresh all countries selected state by localData
     */
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
