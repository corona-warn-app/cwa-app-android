package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.CoronaTest
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
    // TODO Use navargs to supply this
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    val consentGiven = submissionRepository.testForType(type = coronaTestType).map {
        it?.isAdvancedConsentGiven ?: false
    }.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    private val testResultFlow = submissionRepository.testForType(type = coronaTestType)
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

    val testState: LiveData<TestResultUIState> = testResultFlow
        .onEach { testResultUIState ->
            when (val deviceState = testResultUIState.coronaTest.testResult.toDeviceUIState()) {
                DeviceUIState.PAIRED_POSITIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment()
                DeviceUIState.PAIRED_NEGATIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment()
                DeviceUIState.PAIRED_REDEEMED,
                DeviceUIState.PAIRED_ERROR ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment()
                else -> {
                    Timber.w("Unknown success state: %s", deviceState)
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

    val cwaWebExceptionLiveData = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .filter { it.lastError != null }
        .map { it.lastError!! }
        .asLiveData()

    fun deregisterTestFromDevice() = launch {
        Timber.d("deregisterTestFromDevice()")
        submissionRepository.removeTestFromDevice(type = coronaTestType)
        routeToScreen.postValue(null)
    }

    fun refreshDeviceUIState() = launch {
        Timber.v("refreshDeviceUIState()")
        submissionRepository.refreshTest(type = coronaTestType)
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
