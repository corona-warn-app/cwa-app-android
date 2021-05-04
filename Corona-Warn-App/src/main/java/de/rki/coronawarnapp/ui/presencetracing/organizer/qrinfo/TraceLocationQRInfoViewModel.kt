package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TraceLocationQRInfoViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<TraceLocationQRInfoNavigationEvents> = SingleLiveEvent()

    fun openPrivacyCard() {
        routeToScreen.postValue(TraceLocationQRInfoNavigationEvents.NavigateToDataPrivacy)
    }

    fun navigateToMyQRCodes() {
        routeToScreen.postValue(TraceLocationQRInfoNavigationEvents.NavigateToMyQrCodes)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationQRInfoViewModel>
}
