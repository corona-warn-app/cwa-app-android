package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.export.SAFLogExport
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
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
    bugReportingSettings: BugReportingSettings,
    private val logSnapshotter: LogSnapshotter,
    private val safLogExport: SAFLogExport,
    private val contentResolver: ContentResolver,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val isActionInProgress = MutableStateFlow(false)

    val routeToScreen = SingleLiveEvent<DebugLogNavigationEvents>()

    val logUploads = bugReportingSettings.uploadHistory.flow
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

    val errorEvent = SingleLiveEvent<Throwable>()
    val shareEvent = SingleLiveEvent<SAFLogExport.Request>()
    val logStoreResult = SingleLiveEvent<SAFLogExport.Request.Result>()

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onShareButtonPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToShareFragment)
    }

    fun onIdHistoryPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToUploadHistory)
    }

    fun onToggleRecording() = launchWithProgress {
        if (debugLogger.isLogging.value) {
            debugLogger.stop()
        } else {
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

    fun onStoreLog() = launchWithProgress(finishProgressAction = false) {
        Timber.d("storeLog()")
        val snapshot = logSnapshotter.snapshot()
        val shareRequest = safLogExport.createSAFRequest(snapshot)
        shareEvent.postValue(shareRequest)
    }

    fun processSAFResult(requestCode: Int, safPath: Uri?) = launchWithProgress {
        if (safPath == null) {
            Timber.i("No SAF path available.")
            return@launchWithProgress
        }

        val request = safLogExport.getRequest(requestCode)
        if (request == null) {
            Timber.w("Unknown request with code $requestCode")
            return@launchWithProgress
        }

        val storageResult = request.storeSnapshot(contentResolver, safPath)
        Timber.i("Log stored %s", storageResult)

        logStoreResult.postValue(storageResult)
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
                errorEvent.postValue(e)
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

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DebugLogViewModel>
}
