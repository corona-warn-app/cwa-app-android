package de.rki.coronawarnapp.ui.submission.testresult.negative

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class SubmissionTestResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<NavDirections?>()
    val testResult: LiveData<TestResultUIState> = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.testResultReceivedDateFlow
    ) { deviceUiState, resultDate ->
        TestResultUIState(
            deviceUiState = deviceUiState,
            testResultReceivedDate = resultDate
        )
    }.asLiveData(context = dispatcherProvider.Default)

    fun deregisterTestFromDevice() {
        launch {
            Timber.tag(TAG).d("deregisterTestFromDevice()")
            submissionRepository.removeTestFromDevice()

            routeToScreen.postValue(null)
        }
    }

    fun onTestOpened() {
        submissionRepository.setViewedTestResult()
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultNegativeViewModel>

    companion object {
        private const val TAG = "SubmissionTestResult:VM"
    }
}
