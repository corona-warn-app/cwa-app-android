package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.ui.SingleLiveEvent

/**
 * ViewModel for everything Interoperability related
 *
 * @see InteroperabilityRepository
 */
class InteroperabilityViewModel : ViewModel() {
    private val interoperabilityRepository by lazy {
        AppInjector.component.interoperabilityRepository
    }

    val allCountries = interoperabilityRepository.getAllCountries()
    val navigateBack = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun saveInteroperabilityUsed() {
        interoperabilityRepository.saveInteroperabilityUsed()
    }
}
