package de.rki.coronawarnapp.vaccination.ui.consent

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings

class VaccinationConsentViewModel @AssistedInject constructor(
    private val vaccinationSettings: VaccinationSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<VaccinationConsentNavigationEvent>()

    fun onConsentClick() {
        vaccinationSettings.registrationAcknowledged = true
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToQrCodeScan)
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationConsentViewModel>
}
