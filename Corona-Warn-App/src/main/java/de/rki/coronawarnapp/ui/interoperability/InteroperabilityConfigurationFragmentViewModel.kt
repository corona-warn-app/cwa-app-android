package de.rki.coronawarnapp.ui.interoperability

import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class InteroperabilityConfigurationFragmentViewModel @AssistedInject constructor(
    private val interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel() {

    val countryList = MutableLiveData(interoperabilityRepository.getAllCountries())
    val navigateBack = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun saveInteroperabilityUsed() {
        interoperabilityRepository.saveInteroperabilityUsed()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<InteroperabilityConfigurationFragmentViewModel>
}
