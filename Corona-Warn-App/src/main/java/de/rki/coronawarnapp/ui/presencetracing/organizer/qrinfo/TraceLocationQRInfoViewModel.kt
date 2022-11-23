package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TraceLocationQRInfoViewModel @AssistedInject constructor(
    private val traceLocationPreferences: TraceLocationPreferences
) : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<TraceLocationQRInfoNavigationEvents> = SingleLiveEvent()
    val isAlreadyOnboarded = traceLocationPreferences.qrInfoAcknowledged.asLiveData2()

    fun openPrivacyCard() {
        routeToScreen.postValue(TraceLocationQRInfoNavigationEvents.NavigateToDataPrivacy)
    }

    fun navigateToMyQRCodes() {
        routeToScreen.postValue(TraceLocationQRInfoNavigationEvents.NavigateToMyQrCodes)
    }

    fun updateQrInfoAcknowledged(value: Boolean) = launch {
        traceLocationPreferences.updateQrInfoAcknowledged(value)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationQRInfoViewModel>
}
