package de.rki.coronawarnapp.bugreporting.debuglog.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage.UploadHistoryStorage
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class DebugLogViewModel @AssistedInject constructor(
    private val debugLogger: DebugLogger,
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    uploadHistoryStorage: UploadHistoryStorage,
    private val logSnapshotter: LogSnapshotter
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val isActionInProgress = MutableStateFlow(false)

    val logUploads = uploadHistoryStorage.uploadHistory
        .asLiveData(context = dispatcherProvider.Default)

    val state: LiveData<State> = combine(
        isActionInProgress,
        debugLogger.logState
    ) { isActionInProgress, logState ->
        State(
            isRecording = logState.isLogging,
            isLowStorage = logState.isLowStorage,
            currentSize = logState.logSize,
            isActionInProgress = isActionInProgress
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<Event>()

    fun onPrivacyButtonPress() {
        events.postValue(Event.NavigateToPrivacyFragment)
    }

    fun onSendErrorLogPress() {
        events.postValue(Event.NavigateToUploadFragment)
    }

    fun onIdHistoryPress() {
        events.postValue(Event.NavigateToUploadHistory)
    }

    fun onToggleRecording() = launchWithProgress {
        if (debugLogger.isLogging.value) {
            events.postValue(Event.ShowLogDeletionRequest)
        } else {
            if (debugLogger.storageCheck.isLowStorage(forceCheck = true)) {
                Timber.d("Low storage, not starting logger.")
                events.postValue(Event.ShowLowStorageDialog)
                return@launchWithProgress
            }

            debugLogger.start()

            CWADebug.logDeviceInfos()
            try {
                val enfVersion = enfClient.getENFClientVersion()
                Timber.tag("ENFClient").i("ENF Version: %d", enfVersion)
            } catch (e: Exception) {
                Timber.tag("ENFClient").e(e, "Failed to get ENF version for debug log.")
            }
        }
    }

    fun stopAndDeleteDebugLog() {
        launchWithProgress {
            if (debugLogger.isLogging.value) {
                debugLogger.stop()
            }
        }
    }

    fun onExportLogPress() = launchWithProgress(finishProgressAction = true) {
        Timber.d("onExportLogPress()")
        val snapshot = logSnapshotter.snapshot()
        events.postValue(Event.Export(snapshot))
    }

    private fun launchWithProgress(
        finishProgressAction: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        isActionInProgress.value = true

        launch {
            try {
                block()
            } catch (e: Throwable) {
                Timber.e(e, "launchWithProgress() failed.")
                events.postValue(Event.Error(e))
            } finally {
                val duration = System.currentTimeMillis() - startTime
                Timber.v("launchWithProgress() took ${duration}ms")
                if (finishProgressAction) isActionInProgress.value = false
            }
        }
    }

    data class State(
        val isRecording: Boolean,
        val isLowStorage: Boolean,
        val isActionInProgress: Boolean = false,
        val currentSize: Long = 0
    )

    sealed class Event {
        object NavigateToPrivacyFragment : Event()
        object NavigateToUploadFragment : Event()
        object NavigateToUploadHistory : Event()
        object ShowLogDeletionRequest : Event()
        object ShowLowStorageDialog : Event()
        data class Error(val error: Throwable) : Event()
        data class Export(val snapshot: LogSnapshotter.Snapshot) : Event()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogViewModel>
}
