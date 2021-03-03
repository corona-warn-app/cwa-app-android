package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.sharing.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.sharing.SAFLogSharing
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class DebugLogViewModel @AssistedInject constructor(
    private val debugLogger: DebugLogger,
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    bugReportingSettings: BugReportingSettings,
    private val logSnapshotter: LogSnapshotter,
    private val safLogSharing: SAFLogSharing,
    private val contentResolver: ContentResolver,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val logUploads = bugReportingSettings.uploadHistory.flow
        .asLiveData(context = dispatcherProvider.Default)

    private val sharingInProgress = MutableStateFlow(false)

    val routeToScreen = SingleLiveEvent<DebugLogNavigationEvents>()

    val state: LiveData<State> = combine(
        sharingInProgress,
        debugLogger.logState
    ) { sharingInProgress, logState ->
        State(
            isRecording = logState.isLogging,
            isLowStorage = logState.isLowStorage,
            currentSize = logState.logSize,
            sharingInProgress = sharingInProgress
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val errorEvent = SingleLiveEvent<Throwable>()
    val shareEvent = SingleLiveEvent<SAFLogSharing.Request>()

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onIdHistoryPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToUploadHistory)
    }

    fun toggleRecording() = launch {
        try {
            if (debugLogger.isLogging.value) {
                debugLogger.stop()
            } else {
                debugLogger.start()
                printExtendedLogInfos()
            }
        } catch (e: Exception) {
            errorEvent.postValue(e)
        }
    }

    private suspend fun printExtendedLogInfos() {
        CWADebug.logDeviceInfos()
        try {
            val enfVersion = enfClient.getENFClientVersion()
            Timber.tag("ENFClient").i("ENF Version: %d", enfVersion)
        } catch (e: Exception) {
            Timber.tag("ENFClient").e(e, "Failed to get ENF version for debug log.")
        }
    }

    fun shareRecording() {
        sharingInProgress.value = true
        launch {
            try {
                val snapshot = logSnapshotter.snapshot()
                val shareRequest = safLogSharing.createSAFRequest(snapshot)
                shareEvent.postValue(shareRequest)
            } catch (e: Exception) {
                Timber.e(e, "Sharing debug log failed.")
                errorEvent.postValue(e)
            } finally {
                sharingInProgress.value = false
            }
        }
    }

    fun processSAFResult(requestCode: Int, safPath: Uri?) {
        if (safPath == null) {
            errorEvent.postValue(IllegalStateException("Received positive result, but storage path was null"))
            return
        }

        val request = safLogSharing.getRequest(requestCode)
        if (request == null) {
            Timber.w("Unknown request with code $requestCode")
            return
        }

        sharingInProgress.value = true

        launch {
            try {
                request.storeSnapshot(contentResolver, safPath)
                Timber.i("Log stored to %s", safPath)
            } catch (e: Exception) {
                Timber.e(e, "Storing to SAF Uri failed.")
                errorEvent.postValue(e)
            } finally {
                sharingInProgress.value = false
            }
        }
    }

    data class State(
        val isRecording: Boolean,
        val isLowStorage: Boolean,
        val sharingInProgress: Boolean = false,
        val currentSize: Long = 0
    )

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogViewModel>
}
