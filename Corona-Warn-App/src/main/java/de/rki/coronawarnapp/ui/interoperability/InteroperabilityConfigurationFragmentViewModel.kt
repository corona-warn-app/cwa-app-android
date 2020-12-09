package de.rki.coronawarnapp.ui.interoperability

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class InteroperabilityConfigurationFragmentViewModel @AssistedInject constructor(
    private val interopRepo: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val countryList = interopRepo.countryList.asLiveData(context = dispatcherProvider.Default)
    val navigateBack = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun saveInteroperabilityUsed() {
        interopRepo.saveInteroperabilityUsed()
    }

    fun refreshCountries() {
        launch {
            interopRepo.refreshCountries()
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<InteroperabilityConfigurationFragmentViewModel>
}
