package de.rki.coronawarnapp.vaccination.ui.consent

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class VaccinationConsentViewModel @AssistedInject constructor(
    interoperabilityRepository: InteroperabilityRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<VaccinationConsentNavigationEvent>()

    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)

    fun onConsentClick() {
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToQrCodeScan)
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationConsentViewModel>
}
