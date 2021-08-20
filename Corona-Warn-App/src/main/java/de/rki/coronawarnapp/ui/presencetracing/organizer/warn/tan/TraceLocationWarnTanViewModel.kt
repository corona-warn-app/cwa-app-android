package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.TraceLocationWarnDuration
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.tan.Tan
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class TraceLocationWarnTanViewModel @AssistedInject constructor(
    @Assisted private val traceLocationWarnDuration: TraceLocationWarnDuration,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    private val currentTan = MutableStateFlow(Tan(""))

    val state = currentTan.map { currentTan ->
        UIState(
            isTanValid = currentTan.isTanValid,
            isTanValidFormat = currentTan.isTanValidFormat,
            areCharactersCorrect = currentTan.areCharactersValid,
            isCorrectLength = currentTan.isCorrectLength
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val registrationState = MutableLiveData(ApiRequestState.IDLE)
    val registrationError = SingleLiveEvent<CwaWebException>()

    fun onTanChanged(tan: String) {
        currentTan.value = Tan(tan)
    }

    fun startTanSubmission() {
        val teletan = currentTan.value
        if (!teletan.isTanValid) {
            Timber.w("Tried to set invalid teletan: %s", teletan)
            return
        }

        // TODO: some backend call magic here
    }

    data class UIState(
        val isTanValid: Boolean = false,
        val areCharactersCorrect: Boolean = false,
        val isTanValidFormat: Boolean = false,
        val isCorrectLength: Boolean = false
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationWarnTanViewModel> {
        fun create(
            traceLocationWarnDuration: TraceLocationWarnDuration
        ): TraceLocationWarnTanViewModel
    }
}
