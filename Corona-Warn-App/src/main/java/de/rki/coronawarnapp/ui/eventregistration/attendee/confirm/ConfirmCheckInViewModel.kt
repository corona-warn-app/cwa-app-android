package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import org.joda.time.Instant

class ConfirmCheckInViewModel @AssistedInject constructor() : CWAViewModel() {
    private val eventLiveData = MutableLiveData<EventQRCode>()
    val eventData = eventLiveData
    val navigationEvents = SingleLiveEvent<ConfirmCheckInNavigation>()

    fun decodeEvent(encodedEvent: String) = launch {
        try {
            // TODO Verify event(EXPOSUREAPP-5423)
            //  and finalise event parsing logic
            val parseEvent = EventOuterClass.Event.parseFrom(encodedEvent.decodeBase32().toByteArray())
            eventLiveData.postValue(parseEvent.toEventQrCode())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClose() {
        navigationEvents.value = ConfirmCheckInNavigation.BackNavigation
    }

    fun onConfirmEvent() {
        navigationEvents.value = ConfirmCheckInNavigation.ConfirmNavigation
    }

    private fun EventOuterClass.Event.toEventQrCode() = EventQRCode(
        guid = String(guid.toByteArray()),
        description = description,
        start = Instant.ofEpochMilli(start.toLong()),
        end = Instant.ofEpochMilli(end.toLong())
    )

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ConfirmCheckInViewModel>
}
