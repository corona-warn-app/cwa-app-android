package de.rki.coronawarnapp.bugreporting.debuglog.ui.share

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory


class DebugLogShareViewModel @AssistedInject constructor() : CWAViewModel() {
    val routeToScreen: SingleLiveEvent<DebugLogShareNavigationEvents> = SingleLiveEvent()

    fun onXXXPress() {
        routeToScreen.postValue(DebugLogShareNavigationEvents.NavigateToXXXFragment)
    }

    fun onShareButtonPress() {
        // routeToScreen.postValue(DebugLogShareNavigationEvents.NavigateToMainActivity)
    }



    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogShareViewModel>
}
