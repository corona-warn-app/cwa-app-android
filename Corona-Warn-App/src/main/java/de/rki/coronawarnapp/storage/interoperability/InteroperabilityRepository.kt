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
     * Update selected countries by multiple countries at once
     */
    fun updateSelectedCountryCodes(countryCodes: List<String>) {
        val codes = getSelectedCountryCodes()
        codes.toMutableList().addAll(countryCodes)
        LocalData.countryCodes = codes
        selectedCountryCodes.postValue(codes)
    }

    /**
     * Update selected countries by one country
     */
    fun updateSelectedCountryCodes(countryCode: String) {
        val codes = getSelectedCountryCodes()
        codes.toMutableList().add(countryCode)
        LocalData.countryCodes = codes
        selectedCountryCodes.postValue(codes)
    }

}
