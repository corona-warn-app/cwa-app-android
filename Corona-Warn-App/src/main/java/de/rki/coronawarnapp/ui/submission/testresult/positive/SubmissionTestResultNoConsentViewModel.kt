package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers

class SubmissionTestResultNoConsentViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService
) : CWAViewModel() {

    val uiState: LiveData<TestResultUIState> = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.testResultReceivedDateFlow
    ) { deviceUiState, resultDate ->

        TestResultUIState(
            deviceUiState = deviceUiState,
            testResultReceivedDate = resultDate
        )
    }.asLiveData(context = Dispatchers.Default)

    fun onTestOpened() {
        submissionRepository.setViewedTestResult()
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultNoConsentViewModel>
}
