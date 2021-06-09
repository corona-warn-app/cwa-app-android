package de.rki.coronawarnapp.covidcertificate.vaccination.ui.consent

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class VaccinationConsentViewModel @AssistedInject constructor(
    private val vaccinationSettings: VaccinationSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<VaccinationConsentNavigationEvent>()

    fun onConsentClick() {
        vaccinationSettings.registrationAcknowledged = true
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToCertificates)
    }

    fun onDataPrivacyClick() {
        routeToScreen.postValue(VaccinationConsentNavigationEvent.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<VaccinationConsentViewModel>
}
