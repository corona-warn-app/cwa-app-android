package de.rki.coronawarnapp.ui.submission.testresult.invalid

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultInvalidViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    // TODO Use navargs to supply this
    private val coronaTestType: CoronaTest.Type = CoronaTest.Type.PCR

    init {
        Timber.v("init() coronaTestType=%s", coronaTestType)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val testResult: LiveData<TestResultUIState> = submissionRepository.testForType(type = coronaTestType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = dispatcherProvider.Default)

    fun deregisterTestFromDevice() = launch {
        Timber.d("deregisterTestFromDevice()")

        submissionRepository.removeTestFromDevice(type = coronaTestType)
        routeToScreen.postValue(null)
    }

    fun onTestOpened() = launch {
        Timber.d("onTestOpened()")
        submissionRepository.setViewedTestResult(type = coronaTestType)
        testResultAvailableNotificationService.cancelTestResultAvailableNotification()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultInvalidViewModel>
}
