package de.rki.coronawarnapp.ui.eventregistration.checkin

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ConfirmCheckInViewModel @AssistedInject constructor() : CWAViewModel() {
    private val eventLiveData = MutableLiveData<EventOuterClass.Event>()
    val eventData = eventLiveData
    val navigationEvents = SingleLiveEvent<ConfirmCheckInEvent>()

    fun decodeEvent(encodedEvent: String) = launch {
        // TODO (EXPOSUREAPP-5423)
        val decodedEventString = encodedEvent.split(".")[0].decodeBase32()
        val parseEvent = EventOuterClass.Event.parseFrom(decodedEventString.toByteArray())
        eventLiveData.postValue(parseEvent)
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
