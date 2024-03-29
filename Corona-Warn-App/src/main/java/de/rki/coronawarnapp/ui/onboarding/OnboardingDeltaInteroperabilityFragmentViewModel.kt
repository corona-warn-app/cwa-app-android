package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class OnboardingDeltaInteroperabilityFragmentViewModel @AssistedInject constructor(
    private val interopRepo: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val countryList = interopRepo.countryList.asLiveData(context = dispatcherProvider.Default)
    val navigateBack = SingleLiveEvent<Boolean>()

    fun onBackPressed() {
        navigateBack.postValue(true)
    }

    fun saveInteroperabilityUsed() = launch {
        interopRepo.saveInteroperabilityUsed()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OnboardingDeltaInteroperabilityFragmentViewModel>
}
