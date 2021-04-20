package de.rki.coronawarnapp.ui.submission.tan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
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

    val registrationState = MutableLiveData(ApiRequestState.IDLE)
    val registrationError = SingleLiveEvent<CwaWebException>()

    fun onTanChanged(tan: String) {
        currentTan.value = Tan(tan)
    }

    fun onTanSubmit() {
        val teletan = currentTan.value
        if (!teletan.isTanValid) {
            Timber.w("Tried to set invalid teletan: %s", teletan)
            return
        }

        launch {
            try {
                registrationState.postValue(ApiRequestState.STARTED)
                val request = CoronaTestTAN.PCR(tan = teletan.value)
                submissionRepository.registerTest(request)
                registrationState.postValue(ApiRequestState.SUCCESS)
            } catch (err: CwaWebException) {
                registrationState.postValue(ApiRequestState.FAILED)
                registrationError.postValue(err)
            } catch (err: TransactionException) {
                if (err.cause is CwaWebException) {
                    registrationError.postValue(err.cause)
                } else {
                    err.report(ExceptionCategory.INTERNAL)
                }
                registrationState.postValue(ApiRequestState.FAILED)
            } catch (err: Exception) {
                registrationState.postValue(ApiRequestState.FAILED)
                err.report(ExceptionCategory.INTERNAL)
            } finally {
                // TODO Should not be necessary? What new data would we
                submissionRepository.refreshTest(type = CoronaTest.Type.PCR)
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
    interface Factory : SimpleCWAViewModelFactory<SubmissionTanViewModel>
}
