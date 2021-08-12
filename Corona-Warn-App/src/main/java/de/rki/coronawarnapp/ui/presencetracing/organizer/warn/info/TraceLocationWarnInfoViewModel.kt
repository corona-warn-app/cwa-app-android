package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.info

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TraceLocationWarnInfoViewModel @AssistedInject constructor() : CWAViewModel() {
    val proceed = SingleLiveEvent<Unit>()

    fun onProceed() {
        proceed.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationWarnInfoViewModel>
}
