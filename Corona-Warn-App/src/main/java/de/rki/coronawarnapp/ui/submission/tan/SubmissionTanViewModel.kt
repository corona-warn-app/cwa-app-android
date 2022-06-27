package de.rki.coronawarnapp.ui.submission.tan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrTeleTanCensor
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTanViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository
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

    val mutableRegistrationState: MutableLiveData<TanApiRequestState> = MutableLiveData(TanApiRequestState.Idle)
    val registrationState: LiveData<TanApiRequestState> = mutableRegistrationState
    val routeToScreen = SingleLiveEvent<SubmissionNavigationEvents>()
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

        launch {
            PcrTeleTanCensor.addTan(teletan.value)

            val pcrTestAlreadyStored = submissionRepository.testForType(BaseCoronaTest.Type.PCR).first()
            if (pcrTestAlreadyStored != null) {
                val coronaTestTAN = CoronaTestTAN.PCR(tan = teletan.value)
                routeToScreen.postValue(
                    SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromTan(
                        consentGiven = false,
                        coronaTestTan = coronaTestTAN
                    )
                )
            } else {
                onTanSubmit(teletan)
            }
        }
    }

    private suspend fun onTanSubmit(teletan: Tan) {

        try {
            mutableRegistrationState.postValue(TanApiRequestState.Started)
            val request = CoronaTestTAN.PCR(tan = teletan.value)
            val test = submissionRepository.registerTest(request)
            when {
                test.isPositive ->
                    mutableRegistrationState.postValue(TanApiRequestState.SuccessPositiveResult(request.identifier))
                test.isPending ->
                    mutableRegistrationState.postValue(TanApiRequestState.SuccessPendingResult(request.identifier))
            }

        } catch (err: CwaWebException) {
            mutableRegistrationState.postValue(TanApiRequestState.Failure)
            registrationError.postValue(err)
        } catch (err: Exception) {
            mutableRegistrationState.postValue(TanApiRequestState.Failure)
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    data class UIState(
        val isTanValid: Boolean = false,
        val areCharactersCorrect: Boolean = false,
        val isTanValidFormat: Boolean = false,
        val isCorrectLength: Boolean = false
    )

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTanViewModel>

    sealed class TanApiRequestState {
        object Idle : TanApiRequestState()
        object Started : TanApiRequestState()
        object Failure : TanApiRequestState()
        data class SuccessPositiveResult(val identifier: TestIdentifier) : TanApiRequestState()
        data class SuccessPendingResult(val identifier: TestIdentifier) : TanApiRequestState()
    }
}
