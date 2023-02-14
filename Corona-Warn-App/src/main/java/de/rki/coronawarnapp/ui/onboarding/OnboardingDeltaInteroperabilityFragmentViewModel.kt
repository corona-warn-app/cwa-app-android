package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingDeltaInteroperabilityFragmentViewModel @Inject constructor(
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
}
