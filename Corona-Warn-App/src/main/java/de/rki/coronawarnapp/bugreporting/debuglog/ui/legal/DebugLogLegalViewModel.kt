package de.rki.coronawarnapp.bugreporting.debuglog.ui.legal

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogLegalNavigationEvents
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DebugLogLegalViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<DebugLogLegalNavigationEvents> = SingleLiveEvent()

    fun onBackButtonPress() {
        routeToScreen.postValue(DebugLogLegalNavigationEvents.NavigateToShareFragment)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogLegalViewModel>
}
