package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DebugLogUploadViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<NavDirections>()

    fun onUploadLog() {
        // TODO Implement Uploading
    }

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogUploadFragmentDirections.actionDebugLogUploadFragmentToDebugLogLegalFragment())
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogUploadViewModel>
}
