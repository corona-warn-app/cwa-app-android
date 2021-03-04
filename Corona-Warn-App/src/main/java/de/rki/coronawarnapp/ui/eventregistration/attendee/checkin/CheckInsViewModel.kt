package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CheckInsViewModel @AssistedInject constructor(
    private val dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInsViewModel>
}
