package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import timber.log.Timber

class CovidCertificateOnboardingViewModel @AssistedInject constructor(
    private val covidCertificateSettings: CovidCertificateSettings,
    @Assisted private val dccQrCode: DccQrCode?,
    private val dccQrCodeHandler: DccQrCodeHandler,
    dispatcherProvider: DispatcherProvider,
    private val dscRepository: DscRepository,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<Event>()

    init {
        launch {
            val currentDscData = dscRepository.dscData.first()
            if (Duration(currentDscData.updatedAt, timeStamper.nowUTC) < Duration.standardHours(12)) {
                Timber.d("Last DSC data refresh was recent: %s", currentDscData.updatedAt)
                return@launch
            }
            dscRepository.refresh()
        }
    }

    fun onContinueClick() = launch {
        covidCertificateSettings.isOnboarded.update { true }
        val event = if (dccQrCode != null) {
            try {
                val containerId = dccQrCodeHandler.handleQrCode(dccQrCode = dccQrCode)
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

    fun onDataPrivacyClick() {
        events.postValue(Event.NavigateToDataPrivacy)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CovidCertificateOnboardingViewModel> {
        fun create(
            @Assisted dccQrCode: DccQrCode?,
        ): CovidCertificateOnboardingViewModel
    }

    sealed class Event {
        object NavigateToDataPrivacy : Event()
        object NavigateToPersonOverview : Event()
        data class NavigateToDccDetailsScreen(val containerId: CertificateContainerId) : Event()
        data class Error(val throwable: Throwable) : Event()
    }
}
