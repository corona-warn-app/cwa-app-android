package de.rki.coronawarnapp.ui.interoperability

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class InteroperabilityConfigurationFragmentViewModel @Inject constructor(
    private val interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val countryList = interoperabilityRepository.countryList.asLiveData(context = dispatcherProvider.Default)

    val navigateBack = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun saveInteroperabilityUsed() = launch {
        interoperabilityRepository.saveInteroperabilityUsed()
    }
}
