package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.toDeviceUIState
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
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
    private val coronaTestType = MutableLiveData<CoronaTest.Type>()

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = coronaTestType.switchMap { testType ->
        submissionRepository.testForType(type = testType)
            .map { it?.isAdvancedConsentGiven ?: false }
            .asLiveData()
    }

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    fun updateTestType(type: CoronaTest.Type) {
        coronaTestType.postValue(type)
    }

    val testState: LiveData<TestResultUIState> = coronaTestType.switchMap { testType ->
        submissionRepository.testForType(type = testType)
            .filterNotNull()
            .map { test ->
                tokenErrorMutex.withLock {
                    if (!wasRedeemedTokenErrorShown) {
                        if (test.testResult.toDeviceUIState() == DeviceUIState.PAIRED_REDEEMED) {
                            wasRedeemedTokenErrorShown = true
                            showRedeemedTokenWarning.postValue(Unit)
                        }
                    }
                }
                TestResultUIState(coronaTest = test)
            }
            .onEach { testResultUIState ->
                when (val deviceState = testResultUIState.coronaTest.testResult) {
                    CoronaTestResult.PCR_POSITIVE ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment(
                                CoronaTest.Type.PCR
                            )
                    CoronaTestResult.RAT_POSITIVE ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment(
                                CoronaTest.Type.RAPID_ANTIGEN
                            )
                    CoronaTestResult.PCR_NEGATIVE ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment()
                    CoronaTestResult.RAT_NEGATIVE ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionNegativeAntigenTestResultFragment()
                    CoronaTestResult.PCR_REDEEMED, CoronaTestResult.PCR_INVALID ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment(
                                CoronaTest.Type.PCR
                            )
                    CoronaTestResult.RAT_REDEEMED, CoronaTestResult.RAT_INVALID ->
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment(
                                CoronaTest.Type.RAPID_ANTIGEN
                            )
                    else -> {
                        Timber.w("Unknown success state: %s", deviceState)
                        null
                    }
                }?.let { routeToScreen.postValue(it) }
            }
            .filter { testResultUIState ->
                val isPositiveTest = testResultUIState.coronaTest.isSubmissionAllowed
                if (isPositiveTest) {
                    Timber.w("Filtering out positive test emission as we don't display this here.")
                }
                !isPositiveTest
            }
            .asLiveData(context = dispatcherProvider.Default)
    }

    val cwaWebExceptionLiveData = coronaTestType.switchMap { testType ->
        submissionRepository.testForType(type = testType)
            .filterNotNull()
            .filter { it.lastError != null }
            .map { it.lastError!! }
            .asLiveData()
    }

    fun observeTestResultToSchedulePositiveTestResultReminder() = launch {
        coronaTestType.value?.let { testType ->
            submissionRepository.testForType(type = testType)
                .first { request -> request?.isSubmissionAllowed ?: false }
                .also { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() }
        }
    }

    fun deregisterTestFromDevice() = launch {
        Timber.d("deregisterTestFromDevice()")
        coronaTestType.value?.let { submissionRepository.removeTestFromDevice(type = it) }
        routeToScreen.postValue(null)
    }

    fun refreshDeviceUIState() = launch {
        Timber.v("refreshDeviceUIState()")
        coronaTestType.value?.let { submissionRepository.refreshTest(type = it) }
    }

    fun onConsentClicked() {
        routeToScreen.postValue(
            SubmissionTestResultPendingFragmentDirections
                .actionSubmissionResultFragmentToSubmissionYourConsentFragment()
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultPendingViewModel>
}
