package de.rki.coronawarnapp.bugreporting.debuglog.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class DebugLogViewModel @AssistedInject constructor(
    private val debugLogger: DebugLogger,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val ticker = flow {
        while (true) {
            delay(500)
            emit(Unit)
        }
    }
    private val manualTick = MutableStateFlow<Unit>(Unit)

    val state: LiveData<State> = combine(ticker, manualTick) { _, _ ->
        State(
            isRecording = debugLogger.isLogging,
            currentSize = debugLogger.getLogSize()
        )
    }.asLiveData(context = dispatcherProvider.Default)

    fun toggleRecording() {
        launch {
            if (debugLogger.isLogging) debugLogger.stop()
            else debugLogger.start()
            manualTick.value = Unit
        }
    }

    fun shareRecording() {
    }

    data class State(
        val isRecording: Boolean,
        val currentSize: Long = 0,
    )

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<DebugLogViewModel>
}
