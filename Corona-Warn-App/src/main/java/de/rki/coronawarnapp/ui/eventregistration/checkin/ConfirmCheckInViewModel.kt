package de.rki.coronawarnapp.ui.eventregistration.checkin

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class ConfirmCheckInViewModel @AssistedInject constructor(
    private val qrCodeVerifier: QRCodeVerifier
) : CWAViewModel() {
    private val eventLiveData = MutableLiveData<EventQRCode>()
    val eventData = eventLiveData
    val navigationEvents = SingleLiveEvent<ConfirmCheckInEvent>()

    fun decodeEvent(encodedEvent: String) = launch {
        // TODO this logic should moved from here. Here user should confirm event only
        try {
            eventLiveData.postValue(qrCodeVerifier.verify(encodedEvent))
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun onClose() {
        navigationEvents.value = ConfirmCheckInEvent.BackEvent
    }

    fun onConfirmEvent() {
        navigationEvents.value = ConfirmCheckInEvent.ConfirmEvent
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ConfirmCheckInViewModel>
}
