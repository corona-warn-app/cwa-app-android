package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ConfirmCheckInViewModel @AssistedInject constructor() : CWAViewModel() {
    val events = SingleLiveEvent<ConfirmCheckInNavigation>()

    fun onClose() {
        events.value = ConfirmCheckInNavigation.BackNavigation
    }

    fun onConfirmTraceLocation() {
        events.value = ConfirmCheckInNavigation.ConfirmNavigation
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ConfirmCheckInViewModel>
}
