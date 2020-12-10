package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class SubmissionTestResultConsentGivenViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showUploadDialog = submissionRepository.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

    val uiState: LiveData<TestResultUIState> = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.testResultReceivedDateFlow
    ) { deviceUiState, resultDate ->
        TestResultUIState(
            deviceUiState = deviceUiState,
            testResultReceivedDate = resultDate
        )
    }.asLiveData(context = Dispatchers.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showCancelDialog = SingleLiveEvent<Unit>()

    fun onTestOpened() {
        submissionRepository.setViewedTestResult()
    }

    fun onContinuePressed() {
        Timber.d("Beginning symptom flow")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onShowCancelDialog() {
        showCancelDialog.postValue(Unit)
    }

    fun cancelTestSubmission() {
        launch {
            try {
                submissionRepository.startSubmission()
            } catch (e: Exception) {
                Timber.e(e, "cancelTestSubmission() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultConsentGivenViewModel>
}
