package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class TraceLocationQRInfoViewModel @Inject constructor(
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
}
