package de.rki.coronawarnapp.ui.eventregistration.checkin

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
    val navigationEvents = SingleLiveEvent<ConfirmCheckInEvent>()

    fun decodeEvent(encodedEvent: String) = launch {
        // TODO Verify event(EXPOSUREAPP-5423)
        //  and finalise event parsing logic
        val decodedEventString = encodedEvent.split(".")[0].decodeBase32()
        val parseEvent = EventOuterClass.Event.parseFrom(decodedEventString.toByteArray())
        eventLiveData.postValue(parseEvent.toEventQrCode())
    }

    fun onClose() {
        navigationEvents.value = ConfirmCheckInEvent.BackEvent
    }

    fun onConfirmEvent() {
        navigationEvents.value = ConfirmCheckInEvent.ConfirmEvent
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
