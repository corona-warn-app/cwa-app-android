package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DebugLogUploadViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen: SingleLiveEvent<DebugLogUploadNavigationEvents> = SingleLiveEvent()

    fun onUploadLog() {
        // TODO Implement Uploading
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogUploadViewModel>
}
