package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.debuglog.upload.SnapshotUploader
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class DebugLogUploadViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val snapshotUploader: SnapshotUploader
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen: SingleLiveEvent<NavDirections?> = SingleLiveEvent()
    val uploadInProgress = MutableLiveData(false)
    val errorEvent = SingleLiveEvent<Throwable>()
    val uploadSuccess = SingleLiveEvent<String>()

    fun onUploadLog() = launch {
        uploadInProgress.postValue(true)
        try {
            snapshotUploader.uploadSnapshot()
            uploadSuccess.postValue("\uD83D\uDC4D")
            routeToScreen.postValue(null)
        } catch (e: Throwable) {
            errorEvent.postValue(e)
        } finally {
            uploadInProgress.postValue(false)
        }
    }

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogUploadNavigationEvents.NavigateToMoreInformationFragment)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogUploadViewModel>
}
