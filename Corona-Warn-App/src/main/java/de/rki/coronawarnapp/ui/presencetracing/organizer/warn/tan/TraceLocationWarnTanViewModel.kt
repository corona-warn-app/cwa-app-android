package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionException
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionPayload
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionRepository
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
    private val organizerSubmissionRepository: OrganizerSubmissionRepository,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel() {

    private val currentTan = MutableStateFlow(Tan(""))
    val registrationError = SingleLiveEvent<OrganizerSubmissionException>()
    val registrationState = MutableLiveData(ApiRequestState.IDLE)

    val state = currentTan.map { currentTan ->
        UIState(
            isTanValid = currentTan.isTanValid,
            isTanValidFormat = currentTan.isTanValidFormat,
            areCharactersCorrect = currentTan.areCharactersValid,
            isCorrectLength = currentTan.isCorrectLength
        )
    }.asLiveData(context = dispatcherProvider.Default)

    fun onTanChanged(tan: String) {
        currentTan.value = Tan(tan)
    }

    fun startTanSubmission() {
        val teletan = currentTan.value
        if (!teletan.isTanValid) {
            Timber.w("Tried to set invalid teletan: %s", teletan)
            return
        }

        val payload = OrganizerSubmissionPayload(
            traceLocation = traceLocationWarnDuration.traceLocation,
            startDate = traceLocationWarnDuration.dateTime.toDateTime().toInstant(),
            endDate = traceLocationWarnDuration.dateTime.toDateTime()
                .plus(traceLocationWarnDuration.duration).toInstant(),
            tan = teletan.value
        )

        launch {
            try {
                registrationState.postValue(ApiRequestState.STARTED)
                organizerSubmissionRepository.submit(payload)
                registrationState.postValue(ApiRequestState.SUCCESS)
            } catch (err: OrganizerSubmissionException) {
                registrationState.postValue(ApiRequestState.FAILED)
                registrationError.postValue(err)
            } catch (err: Exception) {
                registrationState.postValue(ApiRequestState.FAILED)
                err.report(ExceptionCategory.INTERNAL)
            }
        }
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
