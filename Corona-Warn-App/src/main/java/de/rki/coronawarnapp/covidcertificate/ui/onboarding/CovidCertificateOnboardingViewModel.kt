package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class CovidCertificateOnboardingViewModel @AssistedInject constructor(
    private val covidCertificateSettings: CovidCertificateSettings,
    @Assisted private val dccQrCode: String?,
    private val dccQrCodeExtractor: DccQrCodeExtractor,
    private val dccQrCodeHandler: DccQrCodeHandler,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<Event>()

    fun onContinueClick() = launch {
        covidCertificateSettings.isOnboarded.update { true }
        val event = if (dccQrCode != null) {
            val dccQrCode = dccQrCodeExtractor.extract(dccQrCode)
            val containerId = dccQrCodeHandler.handleQrCode(dccQrCode)
            Event.NavigateToDccDetailsScreen(containerId)
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
            @Assisted dccQrCode: String?,
        ): CovidCertificateOnboardingViewModel
    }

    sealed class Event {
        object NavigateToDataPrivacy : Event()
        object NavigateToPersonOverview : Event()
        data class NavigateToDccDetailsScreen(val containerId: CertificateContainerId) : Event()
    }
}
