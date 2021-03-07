package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ConfirmCheckInViewModel @AssistedInject constructor() : CWAViewModel() {
    private val eventLiveData = MutableLiveData<EventQRCode>()
    val eventData = eventLiveData
    val navigationEvents = SingleLiveEvent<ConfirmCheckInNavigation>()

    fun onClose() {
        navigationEvents.value = ConfirmCheckInNavigation.BackNavigation
    }

    fun onConfirmEvent() {
        navigationEvents.value = ConfirmCheckInNavigation.ConfirmNavigation
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ConfirmCheckInViewModel>
}
