package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CovidCertificateOnboardingViewModel @AssistedInject constructor(
    private val vaccinationSettings: VaccinationSettings,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<Event>()

    fun onContinueClick() {
        vaccinationSettings.registrationAcknowledged = true
        events.postValue(Event.NavigateToPersonOverview)
    }

    fun onDataPrivacyClick() {
        events.postValue(Event.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CovidCertificateOnboardingViewModel>

    sealed class Event {
        object NavigateToDataPrivacy : Event()
        object NavigateToPersonOverview : Event()
    }
}
