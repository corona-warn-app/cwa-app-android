package de.rki.coronawarnapp.ui.submission.testresult.invalid

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
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
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    @Assisted private val testType: CoronaTest.Type
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

    fun deregisterTestFromDevice() = launch {
        Timber.d("deregisterTestFromDevice()")

        submissionRepository.removeTestFromDevice(type = testType)
        routeToScreen.postValue(null)
    }

    fun onTestOpened() = launch {
        Timber.d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = testType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultInvalidViewModel> {
        fun create(testType: CoronaTest.Type): SubmissionTestResultInvalidViewModel
    }
}
