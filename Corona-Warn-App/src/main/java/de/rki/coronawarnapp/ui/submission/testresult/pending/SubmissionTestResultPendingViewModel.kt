package de.rki.coronawarnapp.ui.submission.testresult.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SubmissionTestResultPendingViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    @Assisted private val testIdentifier: TestIdentifier,
    @Assisted private val initialUpdate: Boolean,
    private val coronaTestProvider: CoronaTestProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val coronaTestFlow = coronaTestProvider.getTestForIdentifier(testIdentifier).filterNotNull()

    init {
        Timber.v("init() testIdentifier=%s", testIdentifier)
        if (initialUpdate) {
            updateTestResult()
        }
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()
    val errorEvent = SingleLiveEvent<Throwable>()

    val showRedeemedTokenWarning = SingleLiveEvent<Unit>()

    val consentGiven = coronaTestFlow.map {
        if (it is PersonalCoronaTest) {
            it.isAdvancedConsentGiven
        } else {
            false
        }
    }.asLiveData()

    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    private val testResultFlow = coronaTestFlow.map { test ->
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
            val isFamilyTest = testResultUIState.coronaTest is FamilyCoronaTest
            when (val deviceState = testResultUIState.coronaTest.testResult) {
                CoronaTestResult.PCR_POSITIVE, CoronaTestResult.RAT_POSITIVE -> {
                    if (isFamilyTest) {
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultKeysSharedFragment(
                                testIdentifier = testIdentifier
                            )
                    } else {
                        SubmissionTestResultPendingFragmentDirections
                            .actionSubmissionTestResultPendingFragmentToSubmissionTestResultAvailableFragment(
                                testIdentifier = testIdentifier
                            )
                    }
                }
                CoronaTestResult.PCR_NEGATIVE ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment(
                            testIdentifier = testIdentifier
                        )
                CoronaTestResult.RAT_NEGATIVE -> SubmissionTestResultPendingFragmentDirections
                    .actionSubmissionTestResultPendingFragmentToSubmissionTestResultNegativeFragment(
                        testIdentifier = testIdentifier
                    )
                CoronaTestResult.PCR_OR_RAT_REDEEMED,
                CoronaTestResult.PCR_INVALID,
                CoronaTestResult.RAT_REDEEMED,
                CoronaTestResult.RAT_INVALID ->
                    SubmissionTestResultPendingFragmentDirections
                        .actionSubmissionTestResultPendingFragmentToSubmissionTestResultInvalidFragment(
                            testIdentifier = testIdentifier
                        )
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
                        if (it.coronaTest is FamilyCoronaTest) {
                            R.string.submission_family_test_result_pending_steps_test_certificate_not_available_yet_body
                        } else {
                            R.string.submission_test_result_pending_steps_test_certificate_not_available_yet_body
                        }
                    } else {
                        R.string.submission_test_result_pending_steps_test_certificate_not_desired_by_user_body
                    }
                }
            }.toResolvingString()
        }
        .asLiveData(context = dispatcherProvider.Default)

    val cwaWebExceptionLiveData = coronaTestFlow
        .filterIsInstance<PersonalCoronaTest>()
        .filter { it.lastError != null }
        .map { it.lastError!! }
        .asLiveData()

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        routeToScreen.postValue(null)
    }

    fun updateTestResult() = launch {
        Timber.v("updateTestResult()")
        try {
            coronaTestProvider.refreshTest(coronaTestFlow.first())
        } catch (e: Exception) {
            errorEvent.postValue(e)
        }
    }

    fun onConsentClicked() = launch {
        routeToScreen.postValue(
            SubmissionTestResultPendingFragmentDirections
                .actionSubmissionResultFragmentToSubmissionYourConsentFragment(testType = coronaTestFlow.first().type)
        )
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultPendingViewModel> {
        fun create(
            testIdentifier: TestIdentifier,
            initialUpdate: Boolean
        ): SubmissionTestResultPendingViewModel
    }
}
