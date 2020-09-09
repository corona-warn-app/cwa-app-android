package de.rki.coronawarnapp.storage.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.storage.LocalData

object InteroperabilityRepository {
    private val TAG: String? = InteroperabilityRepository::class.simpleName

    /**
     * LiveData that can be used in a ViewModel for selected country changes
     */
    val selectedCountryCodes = MutableLiveData<List<String>>()

    fun getSelectedCountryCodes(): List<String> =
        LocalData.countryCodes ?: listOf()

    /**
     * Refresh selected country codes state
     */
    fun refreshSelectedCountryCodes() {
        val codes = getSelectedCountryCodes()
        selectedCountryCodes.postValue(codes)
    }

    /**
     * Update selected countries by one country
     */
    fun updateSelectedCountryCodes(countryCode: String, selected: Boolean = true) {
        val codes =
            if (selected) {
                getSelectedCountryCodes() + countryCode
            } else {
                getSelectedCountryCodes() - countryCode
            }
        LocalData.countryCodes = codes
        selectedCountryCodes.postValue(codes)
    }
}
