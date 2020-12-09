package de.rki.coronawarnapp.ui.submission.testresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SubmissionTestResultViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val enfClient: ENFClient,
    private val testResultNotificationService: TestResultNotificationService,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    val showTracingRequiredScreen = SingleLiveEvent<Unit>()
    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = submissionRepository.hasGivenConsentToSubmission.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    val uiState: LiveData<TestResultUIState> = combineTransform(
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
        ).let { emit(it) }
    }.asLiveData(context = dispatcherProvider.Default)

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
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    fun onContinuePressed() {
        Timber.d("onContinuePressed()")
        requireTracingOrShowError {
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
        }
    }

    fun onContinueWithoutSymptoms() {
        Timber.d("onContinueWithoutSymptoms()")
        requireTracingOrShowError {
            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning)
        }
    }

    private fun requireTracingOrShowError(action: () -> Unit) = launch {
        if (enfClient.isTracingEnabled.first()) {
            action()
        } else {
            showTracingRequiredScreen.postValue(Unit)
        }
    }

    fun deregisterTestFromDevice() {
        launch {
            Timber.d("deregisterTestFromDevice()")

            submissionRepository.removeTestFromDevice()

            routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
        }
    }

    fun refreshDeviceUIState(refreshTestResult: Boolean = true) {
        submissionRepository.refreshDeviceUIState(refreshTestResult)
    }

    fun onConsentClicked() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToYourConsent)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultViewModel>
}
