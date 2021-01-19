package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SubmissionTestResultPendingViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val shareTestResultNotificationService: ShareTestResultNotificationService,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = submissionRepository.hasGivenConsentToSubmission.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    private val testResultFlow = combine(
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
    val testState: LiveData<TestResultUIState> = testResultFlow
        .onEach { testResultUIState ->
            testResultUIState.deviceUiState.withSuccess { deviceState ->
                when (deviceState) {
                    DeviceUIState.PAIRED_POSITIVE -> SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment()
                    DeviceUIState.PAIRED_NEGATIVE -> SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment()
                    DeviceUIState.PAIRED_REDEEMED,
                    DeviceUIState.PAIRED_ERROR -> SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment()
                    else -> {
                        Timber.w("Unknown success state: %s", deviceState)
                        null
                    }
                }?.let { routeToScreen.postValue(it) }
            }
        }
        .filter {
            val isPositiveTest = it.deviceUiState is NetworkRequestWrapper.RequestSuccessful &&
                it.deviceUiState.data == DeviceUIState.PAIRED_POSITIVE
            if (isPositiveTest) {
                Timber.w("Filtering out positive test emission as we don't display this here.")
            }
            !isPositiveTest
        }
        .asLiveData(context = dispatcherProvider.Default)

    val cwaWebExceptionLiveData = submissionRepository.deviceUIStateFlow
        .filterIsInstance<NetworkRequestWrapper.RequestFailed<DeviceUIState, CwaWebException>>()
        .map { it.error }
        .asLiveData()

    fun observeTestResultToSchedulePositiveTestResultReminder() = launch {
        submissionRepository.deviceUIStateFlow
            .first { request ->
                request.withSuccess(false) {
                    it == DeviceUIState.PAIRED_POSITIVE || it == DeviceUIState.PAIRED_POSITIVE_TELETAN
                }
            }
            .also { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() }
    }

    fun deregisterTestFromDevice() {
        Timber.d("deregisterTestFromDevice()")
        launch {
            submissionRepository.removeTestFromDevice()
            routeToScreen.postValue(null)
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
