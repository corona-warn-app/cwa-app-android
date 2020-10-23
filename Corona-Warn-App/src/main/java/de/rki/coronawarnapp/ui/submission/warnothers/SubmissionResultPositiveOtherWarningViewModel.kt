package de.rki.coronawarnapp.ui.submission.warnothers

import androidx.lifecycle.asLiveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionResultPositiveOtherWarningViewModel @AssistedInject constructor(
    @Assisted private val symptoms: Symptoms,
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val submissionState = MutableStateFlow(ApiRequestState.IDLE)
    val submissionError = SingleLiveEvent<CwaWebException>()

    val uiState = combineTransform(
        submissionState,
        interoperabilityRepository.countryListFlow
    ) { state, countries ->
        WarnOthersState(
            apiRequestState = state,
            countryList = countries
        ).also { emit(it) }
    }.asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val requestKeySharing = SingleLiveEvent<Unit>()
    val showEnableTracingEvent = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTestResult)
    }

    fun onWarnOthersPressed() {
        launch {
            if (enfClient.isTracingEnabled.first()) {
                requestKeySharing.postValue(Unit)
            } else {
                showEnableTracingEvent.postValue(Unit)
            }
        }
    }

    fun onKeysShared(keys: List<TemporaryExposureKey>) {
        if (keys.isNotEmpty()) {
            submitDiagnosisKeys(keys)
        } else {
            submitWithNoDiagnosisKeys()
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSubmissionDone)
        }
    }

    private fun submitDiagnosisKeys(keys: List<TemporaryExposureKey>) {
        Timber.d("submitDiagnosisKeys(keys=%s, symptoms=%s)", keys, symptoms)

        submissionState.value = ApiRequestState.STARTED
        launch {
            try {
                SubmissionService.asyncSubmitExposureKeys(keys, symptoms)
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSubmissionDone)

                submissionState.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                submissionError.postValue(err)
                submissionState.value = ApiRequestState.FAILED
            } catch (err: TransactionException) {
                if (err.cause is CwaWebException) {
                    submissionError.postValue(err.cause)
                } else {
                    err.report(ExceptionCategory.INTERNAL)
                }
                submissionState.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                submissionState.value = ApiRequestState.FAILED
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    private fun submitWithNoDiagnosisKeys() {
        Timber.d("submitWithNoDiagnosisKeys()")
        SubmissionService.submissionSuccessful()
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionResultPositiveOtherWarningViewModel> {

        fun create(symptoms: Symptoms): SubmissionResultPositiveOtherWarningViewModel
    }
}
