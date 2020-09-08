package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository

/**
 * ViewModel for everything Interoperability related
 *
 * @see InteroperabilityRepository
 */
class InteroperabilityViewModel : ViewModel() {
    val countryCodes = InteroperabilityRepository.selectedCountryCodes

    fun refreshSelectedCountryCodes() {
        InteroperabilityRepository.refreshSelectedCountryCodes()
    }
}
