package de.rki.coronawarnapp.ui.submission.testresult.invalid

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultInvalidViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    @Assisted private val testType: BaseCoronaTest.Type,
    @Assisted private val testIdentifier: TestIdentifier
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val testResult: LiveData<TestResultUIState> = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = dispatcherProvider.Default)

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        routeToScreen.postValue(null)
    }

    fun onTestOpened() = launch {
        Timber.d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = testType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultInvalidViewModel> {
        fun create(testType: BaseCoronaTest.Type, testIdentifier: TestIdentifier): SubmissionTestResultInvalidViewModel
    }
}
