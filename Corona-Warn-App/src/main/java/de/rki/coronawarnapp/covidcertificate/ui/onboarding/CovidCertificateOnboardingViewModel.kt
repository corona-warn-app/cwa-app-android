package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class CovidCertificateOnboardingViewModel @AssistedInject constructor(
    private val covidCertificateSettings: CovidCertificateSettings,
    @Assisted private val certIdentifier: String?,
    @Assisted private val qrcodeSharedViewModel: QrcodeSharedViewModel,
    private val dccQrCodeHandler: DccQrCodeHandler,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<Event>()

    fun onContinueClick() = launch {
        covidCertificateSettings.updateIsOnboarded(true)
        val dccQrCode = certIdentifier?.let { qrcodeSharedViewModel.dccQrCode(it) }
        val event = if (dccQrCode != null) {
            try {
                val containerId = dccQrCodeHandler.validateAndRegister(dccQrCode = dccQrCode)
                Event.NavigateToDccDetailsScreen(containerId)
            } catch (e: Exception) {
                Timber.d(e, "handleQrCode failed")
                Event.Error(e)
            }
        } else {
            Event.NavigateToPersonOverview
        }
        events.postValue(event)
    }

    fun checkOnboardingStatus() = launch {
        if (covidCertificateSettings.isOnboarded.first()) {
            events.postValue(Event.SkipOnboarding)
        }
    }

    fun onDataPrivacyClick() = events.postValue(Event.NavigateToDataPrivacy)

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CovidCertificateOnboardingViewModel> {
        fun create(
            certIdentifier: String?,
            qrcodeSharedViewModel: QrcodeSharedViewModel
        ): CovidCertificateOnboardingViewModel
    }

    sealed class Event {
        object NavigateToDataPrivacy : Event()
        object NavigateToPersonOverview : Event()

        object SkipOnboarding : Event()
        data class NavigateToDccDetailsScreen(val containerId: CertificateContainerId) : Event()
        data class Error(val throwable: Throwable) : Event()
    }
}
