package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.bugreporting.debuglog.upload.SnapshotUploader
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DebugLogUploadViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val snapshotUploader: SnapshotUploader
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    val routeToScreen: SingleLiveEvent<NavDirections?> = SingleLiveEvent()
    val uploadInProgress = MutableLiveData(false)
    val errorEvent = SingleLiveEvent<Unit>()
    val uploadSuccess = SingleLiveEvent<String>()

    fun onUploadLog() = launch {
        uploadInProgress.postValue(true)
        try {
            snapshotUploader.uploadSnapshot()
            uploadSuccess.postValue("\uD83D\uDC4D")
            routeToScreen.postValue(null)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            errorEvent.postValue(Unit)
        } finally {
            uploadInProgress.postValue(false)
        }
    }

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogUploadFragmentDirections.actionDebugLogUploadFragmentToDebugLogLegalFragment())
    }
}

private const val TAG = "DebugLogUploadViewModel"
