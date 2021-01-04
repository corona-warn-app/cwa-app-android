package de.rki.coronawarnapp.bugreporting.debuglog.ui

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.compression.Zipper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.sharing.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.File

class DebugLogViewModel @AssistedInject constructor(
    private val debugLogger: DebugLogger,
    dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val fileSharing: FileSharing
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(500)
        }
    }
    private val manualTick = MutableStateFlow(Unit)
    private val sharingInProgress = MutableStateFlow(false)
    val state: LiveData<State> = combine(ticker, manualTick, sharingInProgress) { _, _, sharingInProgress ->
        State(
            isRecording = debugLogger.isLogging,
            currentSize = debugLogger.getLogSize() + debugLogger.getShareSize(),
            sharingInProgress = sharingInProgress
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val errorEvent = SingleLiveEvent<Throwable>()
    val shareEvent = SingleLiveEvent<Intent>()

    fun toggleRecording() {
        launch {
            if (debugLogger.isLogging) {
                debugLogger.stop()
            } else {
                debugLogger.start()
            }
            manualTick.value = Unit
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

                val sharingIntent = fileSharing.getIntent(
                    path = zipFile,
                    title = zipFile.name
                )

                shareEvent.postValue(sharingIntent)
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
        val sharingInProgress: Boolean = false,
        val currentSize: Long = 0
    )

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<DebugLogViewModel>
}
