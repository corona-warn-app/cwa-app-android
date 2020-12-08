package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SubmissionTestResultPendingViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val testResultNotificationService: TestResultNotificationService,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<NavDirections>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = submissionRepository.hasGivenConsentToSubmission.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    val testResultFlow = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.testResultReceivedDateFlow
    ) { deviceUiState, resultDate ->

        tokenErrorMutex.withLock {
            if (!wasRedeemedTokenErrorShown) {
                deviceUiState.withSuccess {
                    if (it == DeviceUIState.PAIRED_REDEEMED) {
                        wasRedeemedTokenErrorShown = true
                        showRedeemedTokenWarning.postValue(Unit)
                    }
                }
            }
        }

        TestResultUIState(
            deviceUiState = deviceUiState,
            testResultReceivedDate = resultDate
        )
    }
    val uiState: LiveData<TestResultUIState> = testResultFlow.asLiveData(context = dispatcherProvider.Default)

    fun observeTestResultToSchedulePositiveTestResultReminder() = launch {
        submissionRepository.deviceUIStateFlow
            .first { request ->
                request.withSuccess(false) {
                    it == DeviceUIState.PAIRED_POSITIVE || it == DeviceUIState.PAIRED_POSITIVE_TELETAN
                }
            }
            .also { testResultNotificationService.schedulePositiveTestResultReminder() }
    }

    fun onBackPressed() {
        routeToScreen.postValue(
            SubmissionTestResultPendingFragmentDirections
                .actionSubmissionResultFragmentToMainFragment()
        )
    }

//    fun onContinuePressed() {
//        Timber.tag(TAG).d("onContinuePressed()")
//        requireTracingOrShowError {
////            routeToScreen.postValue(
////                SubmissionTestResultPendingFragmentDirections
////                    .actionSubmissionResultFragmentToSubmissionSymptomIntroductionFragment()
////            )
//        }
//    }
//
//    fun onContinueWithoutSymptoms() {
//        Timber.tag(TAG).d("onContinueWithoutSymptoms()")
//        requireTracingOrShowError {
////            routeToScreen.postValue(
////                SubmissionTestResultPendingFragmentDirections
////                    .actionSubmissionResultFragmentToSubmissionResultPositiveOtherWarningFragment()
////            )
//        }
//    }
//
//    private fun requireTracingOrShowError(action: () -> Unit) = launch {
//        if (enfClient.isTracingEnabled.first()) {
//            action()
//        } else {
//            showTracingRequiredScreen.postValue(Unit)
//        }
//    }

    fun deregisterTestFromDevice() {
        launch {
            Timber.tag(TAG).d("deregisterTestFromDevice()")
            submissionRepository.deleteTestGUID()
            submissionRepository.revokeConsentToSubmission()
            SubmissionRepository.deleteRegistrationToken()
            LocalData.isAllowedToSubmitDiagnosisKeys(false)
            LocalData.initialTestResultReceivedTimestamp(0L)

            routeToScreen.postValue(
                SubmissionTestResultPendingFragmentDirections
                    .actionSubmissionResultFragmentToMainFragment()
            )
        }
    }

    fun refreshDeviceUIState(refreshTestResult: Boolean = true) {
        submissionRepository.refreshDeviceUIState(refreshTestResult)
    }

    fun onConsentClicked() {
        routeToScreen.postValue(
            SubmissionTestResultPendingFragmentDirections
                .actionSubmissionResultFragmentToSubmissionYourConsentFragment()
        )
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultPendingViewModel>

    companion object {
        private const val TAG = "SubmissionTestResult:VM"
    }
}
