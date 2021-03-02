package de.rki.coronawarnapp.bugreporting.debuglog.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.compression.Zipper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.sharing.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.File

class DebugLogViewModel @AssistedInject constructor(
    private val debugLogger: DebugLogger,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val fileSharing: FileSharing,
    private val enfClient: ENFClient
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val sharingInProgress = MutableStateFlow(false)

    val routeToScreen = SingleLiveEvent<DebugLogNavigationEvents>()

    val state: LiveData<State> = combine(
        sharingInProgress,
        debugLogger.logState
    ) { sharingInProgress, logState ->
        State(
            isRecording = logState.isLogging,
            isLowStorage = logState.isLowStorage,
            currentSize = logState.logSize + debugLogger.getShareSize(),
            sharingInProgress = sharingInProgress
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val errorEvent = SingleLiveEvent<Throwable>()
    val shareEvent = SingleLiveEvent<FileSharing.ShareIntentProvider>()

    fun onPrivacyButtonPress() {
        routeToScreen.postValue(DebugLogNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onIdHistoryPress() {
        // TODO Add Navigation
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
                debugLogger.clearSharedFiles()

                val now = timeStamper.nowUTC
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val formattedFileName = "CWA Log ${now.toString(formatter)}"
                val zipFile = File(debugLogger.sharedDirectory, "$formattedFileName.zip")

                Zipper(zipFile).zip(
                    listOf(Zipper.Entry(name = "$formattedFileName.txt", path = debugLogger.runningLog))
                )

                val intentProvider = fileSharing.getIntentProvider(
                    path = zipFile,
                    title = zipFile.name,
                    chooserTitle = R.string.debugging_debuglog_sharing_dialog_title
                )

                shareEvent.postValue(intentProvider)
            } catch (e: Exception) {
                Timber.e(e, "Sharing debug log failed.")
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
