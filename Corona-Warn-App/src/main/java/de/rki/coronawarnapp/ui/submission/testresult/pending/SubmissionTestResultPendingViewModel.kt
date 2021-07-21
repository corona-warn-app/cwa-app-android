package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.toDeviceUIState
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SubmissionTestResultPendingViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    @Assisted private val testType: CoronaTest.Type,
    @Assisted private val initialUpdate: Boolean
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
        if (initialUpdate) {
            updateTestResult()
        }
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()
    val errorEvent = SingleLiveEvent<Throwable>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = submissionRepository.testForType(type = testType).map {
        it?.isAdvancedConsentGiven ?: false
    }.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    private val testResultFlow = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { test ->
            tokenErrorMutex.withLock {
                if (!wasRedeemedTokenErrorShown && test.testResult.toDeviceUIState() == DeviceUIState.PAIRED_REDEEMED) {
                    wasRedeemedTokenErrorShown = true
                    showRedeemedTokenWarning.postValue(Unit)
                }
            }
            TestResultUIState(coronaTest = test)
        }

    val testState: LiveData<TestResultUIState> = testResultFlow
        .onEach { testResultUIState ->
            when (val deviceState = testResultUIState.coronaTest.testResult) {
                CoronaTestResult.PCR_POSITIVE, CoronaTestResult.RAT_POSITIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment(testType)
                CoronaTestResult.PCR_NEGATIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment(testType)
                CoronaTestResult.RAT_NEGATIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionNegativeAntigenTestResultFragment()
                CoronaTestResult.PCR_OR_RAT_REDEEMED,
                CoronaTestResult.PCR_INVALID,
                CoronaTestResult.RAT_REDEEMED,
                CoronaTestResult.RAT_INVALID ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment(testType)
                else -> {
                    Timber.w("Unknown success state: $deviceState")
                    null
                }
            }?.let { routeToScreen.postValue(it) }
        }
        .filter { testResultUIState ->
            val isPositiveTest = testResultUIState.coronaTest.isPositive
            if (isPositiveTest) {
                Timber.w("Filtering out positive test emission as we don't display this here.")
            }
            !isPositiveTest
        }
        .asLiveData(context = dispatcherProvider.Default)

    val testCertResultInfo: LiveData<LazyString> = testResultFlow
        .map {
            when {
                !it.coronaTest.isDccSupportedByPoc -> {
                    R.string.submission_test_result_pending_steps_test_certificate_not_supported_body
                }
                else -> {
                    if (it.coronaTest.isDccConsentGiven) {
                        R.string.submission_test_result_pending_steps_test_certificate_not_available_yet_body
                    } else {
                        R.string.submission_test_result_pending_steps_test_certificate_not_desired_by_user_body
                    }
                }
            }.toResolvingString()
        }
        .asLiveData(context = dispatcherProvider.Default)

    val cwaWebExceptionLiveData = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .filter { it.lastError != null }
        .map { it.lastError!! }
        .asLiveData()

    fun deregisterTestFromDevice() = launch {
        Timber.d("deregisterTestFromDevice()")
        submissionRepository.removeTestFromDevice(type = testType)
        routeToScreen.postValue(null)
    }

    fun updateTestResult() = launch {
        Timber.v("updateTestResult()")
        try {
            submissionRepository.refreshTest(type = testType)
        } catch (e: Exception) {
            errorEvent.postValue(e)
        }
    }

    fun onConsentClicked() {
        routeToScreen.postValue(
            SubmissionTestResultPendingFragmentDirections
                .actionSubmissionResultFragmentToSubmissionYourConsentFragment(testType = testType)
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultPendingViewModel> {
        fun create(testType: CoronaTest.Type, initialUpdate: Boolean): SubmissionTestResultPendingViewModel
    }
}
