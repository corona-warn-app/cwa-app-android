package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
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
    private val autoSubmission: AutoSubmission,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val showUploadDialog = autoSubmission.isSubmissionRunning
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
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    fun onContinuePressed() {
        Timber.d("Beginning symptom flow")
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onShowCancelDialog() {
        showCancelDialog.postValue(Unit)
    }

    fun onCancelConfirmed() {
        launch {
            try {
                autoSubmission.runSubmissionNow()
            } catch (e: Exception) {
                Timber.e(e, "onCancelConfirmed() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultConsentGivenViewModel>
}
