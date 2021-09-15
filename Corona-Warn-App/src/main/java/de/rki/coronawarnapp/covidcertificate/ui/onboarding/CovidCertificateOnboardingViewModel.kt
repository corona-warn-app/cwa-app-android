package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.qrcode.ui.DccResult
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class CovidCertificateOnboardingViewModel @AssistedInject constructor(
    private val covidCertificateSettings: CovidCertificateSettings,
    @Assisted("dccType") private val dccType: String?,
    @Assisted("certIdentifier") private val certIdentifier: String?,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<Event>()

    fun onContinueClick() {
        covidCertificateSettings.isOnboarded.update { true }
        val type = DccResult.Type.ofString(dccType)
        val event = if (type != null && certIdentifier != null) {
            Event.NavigateToDccDetailsScreen(type, certIdentifier)
        } else {
            Event.NavigateToPersonOverview
        }
        events.postValue(event)
    }

    fun onDataPrivacyClick() {
        events.postValue(Event.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CovidCertificateOnboardingViewModel> {
        fun create(
            @Assisted("dccType") dccType: String?,
            @Assisted("certIdentifier") certIdentifier: String?,
        ): CovidCertificateOnboardingViewModel
    }

    sealed class Event {
        object NavigateToDataPrivacy : Event()
        object NavigateToPersonOverview : Event()
        data class NavigateToDccDetailsScreen(val type: DccResult.Type, val certIdentifier: String) : Event()
    }
}
